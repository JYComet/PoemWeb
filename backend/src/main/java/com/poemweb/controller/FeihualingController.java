package com.poemweb.controller;

import com.poemweb.service.FeihualingService;
import com.poemweb.service.GraphDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FeihualingController {
    @Autowired
    private FeihualingService feihualingService;

    @Autowired
    private GraphDbService graphDbService;

    // 飞花令 - 按关键字返回含该字的诗句（使用知识图谱检索）
    @GetMapping("/feihualing")
    public Map<String, Object> feihualing(@RequestParam("char") String charInput) {
        Map<String, Object> result = new HashMap<>();
        if (charInput == null || charInput.length() != 1) {
            result.put("success", false);
            result.put("message", "请输入一个汉字");
            return result;
        }

        // 使用知识图谱检索
        List<Map<String, Object>> lines = graphDbService.getFeihualingLinesByKeyword(charInput);

        // 如果知识图谱没有结果，回退到传统方法
        if (lines.isEmpty()) {
            List<FeihualingService.FeihualingLine> fallbackLines = feihualingService.getFeihualingLines(charInput);
            result.put("success", true);
            result.put("data", fallbackLines);
            result.put("char", charInput);
            result.put("source", "traditional");
        } else {
            result.put("success", true);
            result.put("data", lines);
            result.put("char", charInput);
            result.put("source", "knowledge_graph");
        }
        return result;
    }
}