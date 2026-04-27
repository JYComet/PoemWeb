package com.poemweb.controller;

import com.poemweb.entity.Poem;
import com.poemweb.service.PoemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/poems")
public class PoemController {
    @Autowired
    private PoemService poemService;

    // 多维度诗词检索
    @GetMapping("/search")
    public Map<String, Object> searchPoems(
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "author", defaultValue = "") String author,
            @RequestParam(value = "dynasty", defaultValue = "") String dynasty,
            @RequestParam(value = "tag", defaultValue = "") String tag
    ) {
        List<Poem> poems = poemService.searchPoems(keyword, author, dynasty, tag);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", poems);
        result.put("total", poems.size());
        return result;
    }

    // 通过标题搜索诗词（支持AI检索补充）
    @GetMapping("/searchByTitle")
    public Map<String, Object> searchPoemByTitle(@RequestParam("title") String title) {
        Map<String, Object> result = new HashMap<>();
        Poem poem = poemService.searchPoemByTitleWithAI(title);
        
        if (poem != null) {
            result.put("success", true);
            result.put("data", poem);
            result.put("message", poem.getDataSource() == 1 ? "通过AI检索找到并新增" : "从数据库找到");
        } else {
            result.put("success", false);
            result.put("message", "未找到相关诗词");
        }
        return result;
    }

    // 获取诗词详情（含深度解析，支持AI检索）
    @GetMapping("/detail")
    public Map<String, Object> getPoemDetail(@RequestParam("title") String title) {
        Poem poem = poemService.searchPoemByTitleWithAI(title);
        Map<String, Object> result = new HashMap<>();
        if (poem != null) {
            result.put("success", true);
            result.put("data", poem);
            result.put("dataSource", poem.getDataSource());
        } else {
            result.put("success", false);
            result.put("message", "未找到该诗词");
        }
        return result;
    }
}