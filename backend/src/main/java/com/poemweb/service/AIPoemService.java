package com.poemweb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class AIPoemService {

    @Value("${ai.api.key:sk-71985f05958543d88c2df9e81950b0c0}")
    private String apiKey;

    @Value("${ai.api.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Value("${ai.api.model:qwen-turbo}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private GraphDbService graphDbService;

    // 基于知识图谱的智能问答
    public String answerQuestion(String question) {
        // 首先从知识图谱中检索相关信息
        List<Map<String, Object>> kgResults = graphDbService.searchPoemsByKeyword(question);

        String kgContext = buildKnowledgeGraphContext(kgResults, question);

        // 构建系统提示词
        String systemPrompt = "你是一个专业的古诗词问答助手，基于知识图谱数据来回答用户的问题。请根据提供的知识图谱信息，给出准确、详细的回答。如果没有相关知识图谱信息，请根据你的古诗词知识来回答。回答要简洁明了，富有文化底蕴。";

        // 构建用户消息
        String userMessage;
        if (!kgContext.isEmpty()) {
            userMessage = String.format("基于以下知识图谱信息，回答用户的问题：\n\n知识图谱信息：\n%s\n\n用户问题：%s", kgContext, question);
        } else {
            userMessage = String.format("请回答以下关于古诗词的问题：%s", question);
        }

        // 调用AI API
        return callAIAPI(systemPrompt, userMessage);
    }

    // 构建知识图谱上下文
    private String buildKnowledgeGraphContext(List<Map<String, Object>> kgResults, String question) {
        StringBuilder context = new StringBuilder();

        if (kgResults.isEmpty()) {
            return "";
        }

        context.append("从知识图谱中检索到以下相关信息：\n");

        int count = 0;
        for (Map<String, Object> poem : kgResults) {
            if (count >= 5) break; // 限制上下文长度

            String title = (String) poem.getOrDefault("title", "");
            String author = (String) poem.getOrDefault("author", "");
            String dynasty = (String) poem.getOrDefault("dynasty", "");
            String tag = (String) poem.getOrDefault("tag", "");
            String content = (String) poem.getOrDefault("content", "");

            if (!title.isEmpty()) {
                context.append(String.format("- 《%s》%s(%s) %s题材\n", title, author, dynasty, tag));
                if (!content.isEmpty() && content.length() <= 100) {
                    context.append(String.format("  内容：%s\n", content));
                }
            }
            count++;
        }

        return context.toString();
    }

    // AI生成诗词描述
    public String describePoem(String title, String author, String dynasty, String content) {
        // 从知识图谱获取相关信息
        List<Map<String, Object>> relatedPoems = graphDbService.getRelatedPoems(author);

        String kgContext = "";
        if (!relatedPoems.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("诗人%s是%s时期的诗人，其作品包括：\n", author, dynasty));
            int count = 0;
            for (Map<String, Object> poem : relatedPoems) {
                if (count >= 3) break;
                String poemTitle = (String) poem.getOrDefault("title", "");
                if (!poemTitle.equals(title) && !poemTitle.isEmpty()) {
                    sb.append(String.format("- 《%s》\n", poemTitle));
                    count++;
                }
            }
            kgContext = sb.toString();
        }

        String systemPrompt = "你是一个专业的古诗词鉴赏专家，请为用户详细解析指定的诗词。请从创作背景、意境、艺术手法、情感表达等多个角度进行赏析，语言要优美流畅，富有文化底蕴。";

        String userMessage = String.format(
                "请详细赏析以下诗词：\n\n" +
                "标题：《%s》\n" +
                "作者：%s\n" +
                "朝代：%s\n" +
                "内容：%s\n\n" +
                "请从以下几个方面进行赏析：\n" +
                "1. 创作背景\n" +
                "2. 意境分析\n" +
                "3. 艺术手法\n" +
                "4. 情感表达\n" +
                "5. 文学价值\n\n" +
                "%s",
                title, author, dynasty, content != null ? content : "", kgContext
        );

        return callAIAPI(systemPrompt, userMessage);
    }

    // AI检索古诗词，返回JSON格式的结构化数据
    public String searchPoemByAI(String keyword) {
        String systemPrompt = "你是一个专业的古诗词数据库助手。请根据用户搜索的关键词（可能是诗名、作者或诗句内容），返回最匹配的古诗词信息。请以严格的JSON数组格式返回，不要包含任何其他文字说明。每首诗的格式如下：[{\"title\":\"诗名\",\"author\":\"作者\",\"dynasty\":\"朝代\",\"content\":\"诗词内容\",\"annotation\":\"注释（可选）\",\"translation\":\"译文（可选）\",\"background\":\"创作背景（可选）\",\"emotion\":\"情感主旨（可选）\",\"allusion\":\"典故意象（可选）\",\"tag\":\"题材\"}]。如果有多首相关诗词，返回多条记录。如果完全找不到相关诗词，返回空数组[]。请确保JSON格式正确，所有字段值都是字符串。";

        String userMessage = String.format("请搜索关于以下关键词的古诗词信息：%s", keyword);

        String response = callAIAPI(systemPrompt, userMessage);
        
        // 尝试从AI响应中提取JSON
        return extractJSONFromResponse(response);
    }

    // 从AI响应中提取JSON
    private String extractJSONFromResponse(String response) {
        // 尝试找到JSON数组
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        // 如果找不到数组，尝试找对象
        start = response.indexOf('{');
        end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return "[]";
    }

    // 飞花令AI辅助
    public String feihualingWithAI(String keyword, List<String> existingLines) {
        String systemPrompt = "你是一个飞花令游戏助手，请根据用户给定的关键字，提供含有该关键字的古诗词名句。要求诗句准确无误，并注明出处（诗名和作者）。";

        String existingContext = "";
        if (!existingLines.isEmpty()) {
            existingContext = "用户已经提到的诗句有：\n" + String.join("\n", existingLines) + "\n\n请提供不同的诗句。";
        }

        String userMessage = String.format(
                "请提供含有\"%s\"字的古诗词名句（3-5句），格式为：\"诗句\" —— 《诗名》作者\n\n%s",
                keyword, existingContext
        );

        return callAIAPI(systemPrompt, userMessage);
    }

    // 调用AI API
    private String callAIAPI(String systemPrompt, String userMessage) {
        try {
            String url = baseUrl + "/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);

            Map<String, String> userMessageMap = new HashMap<>();
            userMessageMap.put("role", "user");
            userMessageMap.put("content", userMessage);
            messages.add(userMessageMap);

            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode messageNode = choices.get(0).get("message");
                    if (messageNode != null) {
                        return messageNode.get("content").asText();
                    }
                }
            }

            return "抱歉，AI服务暂时不可用，请稍后重试。";

        } catch (Exception e) {
            System.err.println("调用AI API失败: " + e.getMessage());
            return "抱歉，AI服务暂时不可用，请稍后重试。";
        }
    }
}