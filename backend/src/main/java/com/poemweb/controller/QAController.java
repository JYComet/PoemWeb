package com.poemweb.controller;

import com.poemweb.service.QAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class QAController {
    @Autowired
    private QAService qaService;

    // 自然语言智能问答
    @PostMapping("/qa")
    public Map<String, Object> intelligentQA(@RequestBody Map<String, String> request) {
        String question = request.getOrDefault("question", "").trim();
        Map<String, Object> result = new HashMap<>();
        if (question.isEmpty()) {
            result.put("success", false);
            result.put("message", "请输入问题");
            return result;
        }

        Object answer = qaService.intelligentQA(question);
        if (answer != null) {
            result.put("success", true);
            result.put("data", answer);
        } else {
            result.put("success", false);
            result.put("message", "未找到相关答案，请尝试其他问法");
        }
        return result;
    }

    // AI 生成诗词描述
    @PostMapping("/ai/describe_poem")
    public Map<String, Object> aiDescribePoem(@RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "").trim();
        String author = request.getOrDefault("author", "").trim();
        String dynasty = request.getOrDefault("dynasty", "").trim();
        String content = request.getOrDefault("content", "").trim();

        Map<String, Object> result = new HashMap<>();
        if (title.isEmpty() || author.isEmpty()) {
            result.put("success", false);
            result.put("message", "缺少必要参数");
            return result;
        }

        String description = qaService.aiDescribePoem(title, author, dynasty, content);
        result.put("success", true);
        Map<String, String> data = new HashMap<>();
        data.put("description", description);
        result.put("data", data);
        return result;
    }
}