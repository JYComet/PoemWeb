package com.poemweb.service;

import com.poemweb.entity.GraphEdge;
import com.poemweb.entity.GraphNode;
import com.poemweb.entity.Poem;
import com.poemweb.entity.QuizQuestion;
import com.poemweb.entity.WrongAnswer;
import com.poemweb.mapper.GraphEdgeMapper;
import com.poemweb.mapper.GraphNodeMapper;
import com.poemweb.mapper.PoemMapper;
import com.poemweb.mapper.QuizQuestionMapper;
import com.poemweb.mapper.WrongAnswerMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Autowired
    private PoemMapper poemMapper;

    @Autowired
    private GraphNodeMapper graphNodeMapper;

    @Autowired
    private GraphEdgeMapper graphEdgeMapper;

    @Autowired
    public QuizQuestionMapper questionMapper;
    
    @Autowired
    public WrongAnswerMapper wrongAnswerMapper;

    @Autowired
    private AIPoemService aiPoemService;

    // 生成问答题目
    public List<QuizQuestion> generateQuestions(int count, String type) {
        List<QuizQuestion> questions = new ArrayList<>();

        // 获取所有诗词
        List<Poem> poems = poemMapper.selectList(null);
        Collections.shuffle(poems);

        // 获取知识图谱数据
        List<GraphNode> poetNodes = getNodesByCategory("诗人");
        List<GraphNode> dynastyNodes = getNodesByCategory("朝代");
        List<GraphNode> tagNodes = getNodesByCategory("题材");

        // 生成不同类型题目
        int generated = 0;
        int questionIndex = 0;
        
        while (generated < count && questionIndex < poems.size()) {
            Poem poem = poems.get(questionIndex);
            questionIndex++;
            
            if (poem.getTitle() == null || poem.getContent() == null) continue;

            // 根据类型生成题目
            if (type == null || type.equals("choice")) {
                QuizQuestion q = generateChoiceQuestion(poem, poetNodes, dynastyNodes, tagNodes);
                if (q != null) { questions.add(q); generated++; }
            }
            if (generated < count && (type == null || type.equals("judge"))) {
                QuizQuestion q = generateJudgeQuestion(poem, poetNodes);
                if (q != null) { questions.add(q); generated++; }
            }
            if (generated < count && (type == null || type.equals("fill"))) {
                QuizQuestion q = generateFillQuestion(poem);
                if (q != null) { questions.add(q); generated++; }
            }
        }

        // 保存到数据库
        for (QuizQuestion q : questions) {
            q.setCreateTime(LocalDateTime.now());
            questionMapper.insert(q);
        }

        return questions.subList(0, Math.min(count, questions.size()));
    }

    // 生成选择题
    private QuizQuestion generateChoiceQuestion(Poem poem, List<GraphNode> poets, 
                                                List<GraphNode> dynasties, List<GraphNode> tags) {
        // AI 补充数据（如果 author/dynasty/tag 为空）
        if (poem.getAuthor() == null || poem.getAuthor().isEmpty()) {
            enrichPoemWithAI(poem);
        }

        QuizQuestion question = new QuizQuestion();
        question.setQuestionType("choice");
        question.setRelatedPoem(poem.getTitle());
        question.setKnowledgePoint("作者");

        // 随机生成问题类型
        int qType = new Random().nextInt(3);
        String questionText = "";
        String correctAnswer = "";
        List<String> options = new ArrayList<>();

        switch (qType) {
            case 0: // 作者选择题
                if (poem.getAuthor() == null || poem.getAuthor().isEmpty() || poem.getAuthor().equals("未知")) {
                    return null; // 跳过无作者数据的题目
                }
                questionText = "《" + poem.getTitle() + "》的作者是谁？";
                correctAnswer = poem.getAuthor();
                question.setKnowledgePoint("作者");
                // 生成干扰项
                options.add(correctAnswer);
                final String finalCorrectAnswer1 = correctAnswer;
                List<String> otherAuthors = poets.stream()
                        .map(GraphNode::getName)
                        .filter(n -> !n.equals(finalCorrectAnswer1))
                        .collect(Collectors.toList());
                Collections.shuffle(otherAuthors);
                for (int i = 0; i < Math.min(3, otherAuthors.size()); i++) {
                    options.add(otherAuthors.get(i));
                }
                break;
            case 1: // 朝代选择题
                if (poem.getDynasty() == null || poem.getDynasty().isEmpty() || poem.getDynasty().equals("未知")) {
                    return null;
                }
                questionText = "《" + poem.getTitle() + "》创作于哪个朝代？";
                correctAnswer = poem.getDynasty();
                question.setKnowledgePoint("朝代");
                options.add(correctAnswer);
                final String finalCorrectAnswer2 = correctAnswer;
                List<String> otherDynasties = dynasties.stream()
                        .map(GraphNode::getName)
                        .filter(n -> !n.equals(finalCorrectAnswer2))
                        .collect(Collectors.toList());
                Collections.shuffle(otherDynasties);
                for (int i = 0; i < Math.min(3, otherDynasties.size()); i++) {
                    options.add(otherDynasties.get(i));
                }
                break;
            case 2: // 诗词内容填空选择
                String[] lines = poem.getContent().split("，|。|！|？|；");
                if (lines.length > 0 && lines[0].length() > 5) {
                    String line = lines[0].trim();
                    int blankPos = new Random().nextInt(Math.min(4, line.length() - 2));
                    String answer = line.substring(blankPos, blankPos + 1);
                    questionText = "「" + line.substring(0, blankPos) + "___" + 
                                  line.substring(blankPos + 1) + "」中空缺的字是？";
                    correctAnswer = answer;
                    question.setKnowledgePoint("诗词内容");
                    options.add(answer);
                    options.add("风");
                    options.add("月");
                    options.add("花");
                } else {
                    if (poem.getTag() == null || poem.getTag().isEmpty() || poem.getTag().equals("其他")) {
                        return null;
                    }
                    questionText = "《" + poem.getTitle() + "》的题材是什么？";
                    correctAnswer = poem.getTag();
                    question.setKnowledgePoint("题材");
                    options.add(correctAnswer);
                    final String finalCorrectAnswer3 = correctAnswer;
                    List<String> otherTags = tags.stream()
                            .map(GraphNode::getName)
                            .filter(n -> !n.equals(finalCorrectAnswer3))
                            .collect(Collectors.toList());
                    Collections.shuffle(otherTags);
                    for (int i = 0; i < Math.min(3, otherTags.size()); i++) {
                        options.add(otherTags.get(i));
                    }
                }
                break;
        }

        // 确保选项数量为4个
        while (options.size() < 4) {
            options.add("其他");
        }
        Collections.shuffle(options);

        question.setQuestion(questionText);
        question.setAnswer(correctAnswer);
        question.setOptions(String.join(",", options));

        return question;
    }

    // AI 补充诗词数据
    private void enrichPoemWithAI(Poem poem) {
        try {
            String jsonResult = aiPoemService.searchPoemByAI(poem.getTitle());
            if (jsonResult != null && jsonResult.startsWith("[")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                List<Map<String, String>> results = mapper.readValue(jsonResult, 
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, String>>>(){});
                if (results != null && !results.isEmpty()) {
                    Map<String, String> aiData = results.get(0);
                    boolean updated = false;
                    if (aiData.containsKey("author") && aiData.get("author") != null && !aiData.get("author").isEmpty()) {
                        poem.setAuthor(aiData.get("author"));
                        updated = true;
                    }
                    if (aiData.containsKey("dynasty") && aiData.get("dynasty") != null && !aiData.get("dynasty").isEmpty()) {
                        poem.setDynasty(aiData.get("dynasty"));
                        updated = true;
                    }
                    if (aiData.containsKey("tag") && aiData.get("tag") != null && !aiData.get("tag").isEmpty()) {
                        poem.setTag(aiData.get("tag"));
                        updated = true;
                    }
                    if (updated) {
                        poem.setUpdateTime(java.time.LocalDateTime.now());
                        poemMapper.updateById(poem);
                    }
                }
            }
        } catch (Exception e) {
            // AI 补充失败，静默处理
        }
    }

    // 生成填空题
    private QuizQuestion generateFillQuestion(Poem poem) {
        QuizQuestion question = new QuizQuestion();
        question.setQuestionType("fill");
        question.setRelatedPoem(poem.getTitle());
        question.setKnowledgePoint("诗词默写");

        String[] lines = poem.getContent().split("，|。|！|？|；");
        if (lines.length > 0) {
            String line = lines[0].trim();
            if (line.length() > 3) {
                // 随机挖空
                int blankLen = Math.min(2, line.length() / 3);
                int start = new Random().nextInt(line.length() - blankLen);
                String answer = line.substring(start, start + blankLen);
                String questionText = "「" + line.substring(0, start) + "___" + 
                                     line.substring(start + blankLen) + "」";
                question.setQuestion(questionText + " 请补全空缺部分");
                question.setAnswer(answer);
            }
        }

        if (question.getQuestion() == null) {
            question.setQuestion("《" + poem.getTitle() + "》的下一句是什么？");
            question.setAnswer(poem.getContent());
        }

        return question;
    }

    // 生成判断题
    private QuizQuestion generateJudgeQuestion(Poem poem, List<GraphNode> poets) {
        QuizQuestion question = new QuizQuestion();
        question.setQuestionType("judge");
        question.setRelatedPoem(poem.getTitle());
        question.setKnowledgePoint("诗词常识");

        boolean isTrue = new Random().nextBoolean();
        String correctAuthor = poem.getAuthor() != null ? poem.getAuthor() : "未知";
        
        if (isTrue) {
            question.setQuestion("判断题：《" + poem.getTitle() + "》的作者是" + correctAuthor + "。（ ）");
            question.setAnswer("正确");
        } else {
            // 找一个错误的作者
            List<String> otherAuthors = poets.stream()
                    .map(GraphNode::getName)
                    .filter(n -> !n.equals(correctAuthor))
                    .collect(Collectors.toList());
            String wrongAuthor = otherAuthors.isEmpty() ? "李白" : otherAuthors.get(0);
            question.setQuestion("判断题：《" + poem.getTitle() + "》的作者是" + wrongAuthor + "。（ ）");
            question.setAnswer("错误");
        }

        question.setOptions("正确,错误");
        return question;
    }

    private List<GraphNode> getNodesByCategory(String category) {
        QueryWrapper<GraphNode> wrapper = new QueryWrapper<>();
        wrapper.eq("category", category);
        return graphNodeMapper.selectList(wrapper);
    }

    // 记录错误答案
    public void recordWrongAnswer(Long userId, Long questionId, String question, 
                                  String correctAnswer, String userAnswer, 
                                  String knowledgePoint, String relatedPoem) {
        QueryWrapper<WrongAnswer> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("question_id", questionId);
        WrongAnswer existing = wrongAnswerMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setWrongCount(existing.getWrongCount() + 1);
            existing.setUserAnswer(userAnswer);
            existing.setLastWrongTime(LocalDateTime.now());
            existing.setUpdateTime(LocalDateTime.now());
            wrongAnswerMapper.updateById(existing);
        } else {
            WrongAnswer wrong = new WrongAnswer();
            wrong.setUserId(userId);
            wrong.setQuestionId(questionId);
            wrong.setQuestion(question);
            wrong.setCorrectAnswer(correctAnswer);
            wrong.setUserAnswer(userAnswer);
            wrong.setKnowledgePoint(knowledgePoint);
            wrong.setRelatedPoem(relatedPoem);
            wrong.setWrongCount(1);
            wrong.setLastWrongTime(LocalDateTime.now());
            wrong.setCreateTime(LocalDateTime.now());
            wrong.setUpdateTime(LocalDateTime.now());
            wrongAnswerMapper.insert(wrong);
        }
    }

    // 获取用户错题集
    public List<WrongAnswer> getUserWrongAnswers(Long userId) {
        QueryWrapper<WrongAnswer> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("last_wrong_time");
        return wrongAnswerMapper.selectList(wrapper);
    }

    // 删除错题
    public void removeWrongAnswer(Long userId, Long wrongAnswerId) {
        QueryWrapper<WrongAnswer> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("id", wrongAnswerId);
        wrongAnswerMapper.delete(wrapper);
    }

    // 获取错题统计
    public Map<String, Object> getWrongAnswerStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<WrongAnswer> wrongAnswers = getUserWrongAnswers(userId);
        stats.put("totalWrong", wrongAnswers.size());
        
        // 按知识点统计
        Map<String, Long> knowledgePointStats = wrongAnswers.stream()
                .collect(Collectors.groupingBy(WrongAnswer::getKnowledgePoint, Collectors.counting()));
        stats.put("knowledgePointStats", knowledgePointStats);
        
        // 按错误次数统计
        long highFreqWrong = wrongAnswers.stream()
                .filter(w -> w.getWrongCount() >= 3)
                .count();
        stats.put("highFreqWrong", highFreqWrong);

        return stats;
    }
}
