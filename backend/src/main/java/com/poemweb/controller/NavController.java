package com.poemweb.controller;

import com.poemweb.service.NavService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nav")
public class NavController {
    @Autowired
    private NavService navService;

    // 诗人分类导航
    @GetMapping("/authors")
    public Map<String, Object> navAuthors() {
        List<NavService.AuthorNavItem> authors = navService.getAuthorNav();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", authors);
        return result;
    }

    // 题材分类导航
    @GetMapping("/tags")
    public Map<String, Object> navTags() {
        List<NavService.TagNavItem> tags = navService.getTagNav();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", tags);
        return result;
    }

    // 朝代分类导航
    @GetMapping("/dynasties")
    public Map<String, Object> navDynasties() {
        List<NavService.DynastyNavItem> dynasties = navService.getDynastyNav();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", dynasties);
        return result;
    }
}