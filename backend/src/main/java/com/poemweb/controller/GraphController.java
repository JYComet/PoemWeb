package com.poemweb.controller;

import com.poemweb.service.GraphDbService;
import com.poemweb.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/graph")
public class GraphController {
    @Autowired
    private GraphService graphService;

    @Autowired
    private GraphDbService graphDbService;

    // 知识图谱数据：诗人-朝代-诗词-题材 关系，供 ECharts graph 使用
    @GetMapping("/knowledge")
    public Map<String, Object> knowledgeGraph(
            @RequestParam(value = "limit", defaultValue = "150") int limit
    ) {
        Map<String, Object> graphData = graphDbService.getGraphData(limit);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", graphData);
        return result;
    }

    // 获取特定节点的局部知识图谱（按诗人/朝代/诗词/题材）
    @GetMapping("/subgraph")
    public Map<String, Object> subgraph(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "name", required = false) String name
    ) {
        Map<String, Object> result = new HashMap<>();
        if (type == null || type.isEmpty() || name == null || name.isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供type和name参数");
            return result;
        }

        Map<String, Object> graphData = graphDbService.getSubGraphByNode(type, name);
        result.put("success", true);
        result.put("data", graphData);
        return result;
    }

    // 获取所有可供筛选的节点列表（诗人、朝代、题材、诗词）
    @GetMapping("/nodes")
    public Map<String, Object> getAllNodes() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", graphDbService.getAllNodeCategories());
        return result;
    }

    // 根据分类获取节点列表
    @GetMapping("/nodes/category")
    public Map<String, Object> getNodesByCategory(
            @RequestParam("category") String category
    ) {
        Map<String, Object> result = new HashMap<>();
        List<String> nodes = graphDbService.getNodeNamesByCategory(category);
        result.put("success", true);
        result.put("data", nodes);
        result.put("category", category);
        return result;
    }
}