package com.poemweb.service;

import com.poemweb.entity.Poem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GraphService {
    @Autowired
    private PoemService poemService;

    // 生成知识图谱数据
    public Map<String, Object> getKnowledgeGraph(int limit) {
        limit = Math.min(limit, 300);
        List<Poem> poems = poemService.getAllPoems();

        // 统计并选取高频节点
        Map<String, Integer> authorCount = new HashMap<>();
        Set<String> dynastySet = new HashSet<>();
        Set<String> tagSet = new HashSet<>();
        Set<String> poemTitles = new HashSet<>();

        for (Poem p : poems) {
            authorCount.put(p.getAuthor(), authorCount.getOrDefault(p.getAuthor(), 0) + 1);
            dynastySet.add(p.getDynasty());
            tagSet.add(p.getTag() != null ? p.getTag() : "其他");
            poemTitles.add(p.getTitle());
        }

        // 取作品数较多的诗人
        List<String> topAuthors = new ArrayList<>(authorCount.keySet());
        topAuthors.sort((a, b) -> authorCount.get(b) - authorCount.get(a));
        if (topAuthors.size() > limit / 4) {
            topAuthors = topAuthors.subList(0, limit / 4);
        }

        List<String> dynasties = new ArrayList<>(dynastySet);
        List<String> tags = new ArrayList<>(tagSet);

        // 构建节点和边
        List<Node> nodes = new ArrayList<>();
        Set<String> nodeIds = new HashSet<>();
        List<Category> categories = Arrays.asList(
                new Category(0, "诗人"),
                new Category(1, "朝代"),
                new Category(2, "诗词"),
                new Category(3, "题材")
        );

        // 添加节点
        for (String author : topAuthors) {
            int count = authorCount.get(author);
            addNode(nodes, nodeIds, author, 0, count);
        }

        for (String dynasty : dynasties) {
            addNode(nodes, nodeIds, dynasty, 1, 1);
        }

        for (String tag : tags) {
            addNode(nodes, nodeIds, tag, 3, 1);
        }

        // 添加诗词节点和边
        List<Link> links = new ArrayList<>();
        int poemCount = 0;
        int poemLimit = limit / 2;

        for (Poem p : poems) {
            if (!topAuthors.contains(p.getAuthor())) {
                continue;
            }
            if (poemCount >= poemLimit) {
                break;
            }
            String title = p.getTitle();
            if (nodeIds.contains(title)) {
                continue;
            }
            addNode(nodes, nodeIds, title, 2, 1);
            poemCount++;

            // 添加边
            links.add(new Link(p.getAuthor(), title));
            links.add(new Link(p.getAuthor(), p.getDynasty()));
            links.add(new Link(title, p.getTag() != null ? p.getTag() : "其他"));
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodes);
        result.put("links", links);
        result.put("categories", categories);

        return result;
    }

    // 添加节点
    private void addNode(List<Node> nodes, Set<String> nodeIds, String name, int category, int value) {
        if (name == null || name.isEmpty() || nodeIds.contains(name)) {
            return;
        }
        nodeIds.add(name);
        Node node = new Node();
        node.setName(name);
        node.setCategory(category);
        node.setValue(value);
        node.setSymbolSize(10 + Math.min(value, 50));
        nodes.add(node);
    }

    // 节点类
    public static class Node {
        private String name;
        private int category;
        private int value;
        private int symbolSize;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCategory() {
            return category;
        }

        public void setCategory(int category) {
            this.category = category;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public int getSymbolSize() {
            return symbolSize;
        }

        public void setSymbolSize(int symbolSize) {
            this.symbolSize = symbolSize;
        }
    }

    // 边类
    public static class Link {
        private String source;
        private String target;

        public Link(String source, String target) {
            this.source = source;
            this.target = target;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }

    // 分类类
    public static class Category {
        private int name;
        private String value;

        public Category(int name, String value) {
            this.name = name;
            this.value = value;
        }

        public int getName() {
            return name;
        }

        public void setName(int name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}