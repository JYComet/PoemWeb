package com.poemweb.controller;

import com.poemweb.entity.Poem;
import com.poemweb.service.ReciteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReciteController {
    @Autowired
    private ReciteService reciteService;

    // 诗词背诵 - 随机返回一首诗词（可隐藏部分）
    @GetMapping("/recite")
    public Map<String, Object> recite() {
        Poem poem = reciteService.getRandomPoem();
        Map<String, Object> result = new HashMap<>();
        if (poem != null) {
            result.put("success", true);
            result.put("data", poem);
        } else {
            result.put("success", false);
            result.put("message", "获取诗词失败");
        }
        return result;
    }

    // 诗词默写 - 返回带空白的诗词
    @GetMapping("/dictation")
    public Map<String, Object> dictation() {
        Poem poem = reciteService.getDictationPoem();
        Map<String, Object> result = new HashMap<>();
        if (poem != null) {
            result.put("success", true);
            result.put("data", poem);
        } else {
            result.put("success", false);
            result.put("message", "获取诗词失败");
        }
        return result;
    }
}