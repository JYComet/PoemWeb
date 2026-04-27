package com.poemweb.controller;

import com.poemweb.entity.QuizQuestion;
import com.poemweb.entity.WrongAnswer;
import com.poemweb.service.QuizService;
import com.poemweb.mapper.QuizQuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;
    
    @Autowired
    private QuizQuestionMapper questionMapper;

    // 生成问答题目
    @PostMapping("/generate")
    public Map<String, Object> generateQuestions(@RequestBody Map<String, Object> params, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        int count = 10;
        if (params.containsKey("count") && params.get("count") != null) {
            try {
                count = ((Number) params.get("count")).intValue();
            } catch (Exception e) {
                count = 10;
            }
        }
        String type = params.containsKey("type") ? (String) params.get("type") : null;

        try {
            List<QuizQuestion> questions = quizService.generateQuestions(count, type);
            result.put("success", true);
            result.put("message", "生成成功");
            result.put("data", questions);
            result.put("total", questions.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "生成问题失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    // 提交答案
    @PostMapping("/submit")
    public Map<String, Object> submitAnswer(@RequestBody Map<String, Object> params, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        Long questionId = params.containsKey("questionId") ? Long.valueOf(params.get("questionId").toString()) : null;
        String userAnswer = params.containsKey("userAnswer") ? (String) params.get("userAnswer") : "";
        
        if (questionId == null) {
            result.put("success", false);
            result.put("message", "题目ID不能为空");
            return result;
        }

        QuizQuestion question = quizService.questionMapper.selectById(questionId);
        if (question == null) {
            result.put("success", false);
            result.put("message", "题目不存在");
            return result;
        }

        boolean isCorrect = question.getAnswer().equals(userAnswer.trim());
        
        if (!isCorrect) {
            quizService.recordWrongAnswer(userId, questionId, question.getQuestion(),
                    question.getAnswer(), userAnswer, question.getKnowledgePoint(), 
                    question.getRelatedPoem());
        }

        result.put("success", true);
        result.put("correct", isCorrect);
        result.put("correctAnswer", question.getAnswer());
        result.put("message", isCorrect ? "回答正确！" : "回答错误，已加入错题集");
        
        return result;
    }

    // 获取用户错题集
    @GetMapping("/wrongAnswers")
    public Map<String, Object> getWrongAnswers(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        List<WrongAnswer> wrongAnswers = quizService.getUserWrongAnswers(userId);
        
        result.put("success", true);
        result.put("data", wrongAnswers);
        result.put("total", wrongAnswers.size());
        
        return result;
    }

    // 删除错题
    @PostMapping("/wrongAnswers/remove")
    public Map<String, Object> removeWrongAnswer(@RequestBody Map<String, Object> params, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        Long wrongAnswerId = params.containsKey("id") ? Long.valueOf(params.get("id").toString()) : null;
        if (wrongAnswerId == null) {
            result.put("success", false);
            result.put("message", "错题ID不能为空");
            return result;
        }

        quizService.removeWrongAnswer(userId, wrongAnswerId);
        
        result.put("success", true);
        result.put("message", "已移除");
        
        return result;
    }

    // 获取错题统计
    @GetMapping("/stats")
    public Map<String, Object> getStats(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        Map<String, Object> stats = quizService.getWrongAnswerStats(userId);
        
        result.put("success", true);
        result.put("data", stats);
        
        return result;
    }
}
