package com.poemweb.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poemweb.entity.GraphEdge;
import com.poemweb.entity.GraphNode;
import com.poemweb.entity.Poem;
import com.poemweb.mapper.GraphEdgeMapper;
import com.poemweb.mapper.GraphNodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraphDbService {

    @Autowired
    private GraphNodeMapper graphNodeMapper;

    @Autowired
    private GraphEdgeMapper graphEdgeMapper;

    @Autowired
    private PoemService poemService;

    // 构建知识图谱数据
    public void buildKnowledgeGraph() {
        System.out.println("开始构建知识图谱...");

        List<Poem> poems = poemService.getAllPoems();

        Map<String, Long> authorNodeMap = new HashMap<>();
        Map<String, Long> dynastyNodeMap = new HashMap<>();
        Map<String, Long> tagNodeMap = new HashMap<>();
        Map<String, Long> poemNodeMap = new HashMap<>();

        int count = 0;
        for (Poem poem : poems) {
            // 创建作者节点
            Long authorId = getOrCreateNode(authorNodeMap, poem.getAuthor(), "诗人",
                    String.format("{\"name\":\"%s\",\"dynasty\":\"%s\"}", poem.getAuthor(), poem.getDynasty()));

            // 创建朝代节点
            Long dynastyId = getOrCreateNode(dynastyNodeMap, poem.getDynasty(), "朝代",
                    String.format("{\"name\":\"%s\"}", poem.getDynasty()));

            // 创建题材节点
            String tag = poem.getTag() != null ? poem.getTag() : "其他";
            Long tagId = getOrCreateNode(tagNodeMap, tag, "题材",
                    String.format("{\"name\":\"%s\"}", tag));

            // 创建诗词节点
            String poemProperties = String.format(
                    "{\"title\":\"%s\",\"author\":\"%s\",\"dynasty\":\"%s\",\"tag\":\"%s\",\"content\":\"%s\"}",
                    poem.getTitle(), poem.getAuthor(), poem.getDynasty(), tag,
                    poem.getContent() != null ? poem.getContent().replace("\"", "\\\"") : "");

            Long poemId = getOrCreateNode(poemNodeMap, poem.getTitle(), "诗词", poemProperties);

            // 创建边：作者 -> 诗词
            createEdge(authorId, poemId, "创作");

            // 创建边：诗词 -> 朝代
            createEdge(poemId, dynastyId, "属于朝代");

            // 创建边：诗词 -> 题材
            createEdge(poemId, tagId, "属于题材");

            count++;
            if (count % 100 == 0) {
                System.out.println("已处理 " + count + " 首诗词");
            }
        }

        System.out.println("知识图谱构建完成，共导入 " + graphNodeMapper.selectCount(null) + " 个节点，" +
                graphEdgeMapper.selectCount(null) + " 条边");
    }

    // 获取或创建节点
    private Long getOrCreateNode(Map<String, Long> cache, String name, String category, String properties) {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }

        QueryWrapper<GraphNode> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name).eq("category", category);
        GraphNode existing = graphNodeMapper.selectOne(wrapper);

        if (existing != null) {
            cache.put(name, existing.getId());
            return existing.getId();
        }

        GraphNode node = new GraphNode();
        node.setName(name);
        node.setCategory(category);
        node.setProperties(properties);
        node.setCreateTime(LocalDateTime.now());
        node.setUpdateTime(LocalDateTime.now());
        graphNodeMapper.insert(node);

        cache.put(name, node.getId());
        return node.getId();
    }

    // 创建边
    private void createEdge(Long sourceId, Long targetId, String relation) {
        QueryWrapper<GraphEdge> wrapper = new QueryWrapper<>();
        wrapper.eq("source_id", sourceId).eq("target_id", targetId).eq("relation", relation);
        Long count = graphEdgeMapper.selectCount(wrapper);

        if (count == 0) {
            GraphEdge edge = new GraphEdge();
            edge.setSourceId(sourceId);
            edge.setTargetId(targetId);
            edge.setRelation(relation);
            edge.setProperties("{}");
            edge.setCreateTime(LocalDateTime.now());
            edge.setUpdateTime(LocalDateTime.now());
            graphEdgeMapper.insert(edge);
        }
    }

    // 根据关键词搜索相关诗词
    public List<Map<String, Object>> searchPoemsByKeyword(String keyword) {
        List<GraphNode> nodes = graphNodeMapper.searchByName(keyword);

        Set<Long> poemIds = new HashSet<>();
        for (GraphNode node : nodes) {
            if ("诗词".equals(node.getCategory())) {
                poemIds.add(node.getId());
            } else if ("诗人".equals(node.getCategory())) {
                // 查找该诗人创作的诗词
                List<GraphEdge> edges = graphEdgeMapper.findBySourceId(node.getId());
                for (GraphEdge edge : edges) {
                    if ("创作".equals(edge.getRelation())) {
                        poemIds.add(edge.getTargetId());
                    }
                }
            } else if ("题材".equals(node.getCategory())) {
                // 查找该题材下的诗词
                List<GraphEdge> edges = graphEdgeMapper.findByTargetId(node.getId());
                for (GraphEdge edge : edges) {
                    if ("属于题材".equals(edge.getRelation())) {
                        poemIds.add(edge.getSourceId());
                    }
                }
            } else if ("朝代".equals(node.getCategory())) {
                // 查找该朝代的诗词
                List<GraphEdge> edges = graphEdgeMapper.findByTargetId(node.getId());
                for (GraphEdge edge : edges) {
                    if ("属于朝代".equals(edge.getRelation())) {
                        poemIds.add(edge.getSourceId());
                    }
                }
            }
        }

        // 获取诗词详情
        List<Map<String, Object>> results = new ArrayList<>();
        for (Long poemId : poemIds) {
            QueryWrapper<GraphNode> wrapper = new QueryWrapper<>();
            wrapper.eq("id", poemId).eq("category", "诗词");
            GraphNode poemNode = graphNodeMapper.selectOne(wrapper);

            if (poemNode != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> properties = mapper.readValue(poemNode.getProperties(), Map.class);
                    results.add(properties);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return results;
    }

    // 根据关键词获取含该字的诗句（用于飞花令）
    public List<Map<String, Object>> getFeihualingLinesByKeyword(String keyword) {
        QueryWrapper<GraphNode> wrapper = new QueryWrapper<>();
        wrapper.eq("category", "诗词");
        List<GraphNode> poemNodes = graphNodeMapper.selectList(wrapper);

        List<Map<String, Object>> results = new ArrayList<>();
        for (GraphNode poemNode : poemNodes) {
            if (poemNode.getProperties() != null && poemNode.getProperties().contains(keyword)) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> properties = mapper.readValue(poemNode.getProperties(), Map.class);
                    String content = (String) properties.get("content");
                    if (content != null && content.contains(keyword)) {
                        // 分割诗句
                        String[] lines = content.replace("。", "。\n").replace("，", "，\n")
                                .replace("？", "？\n").replace("！", "！\n").split("\n");

                        for (String line : lines) {
                            line = line.trim();
                            if (!line.isEmpty() && line.contains(keyword) && line.length() <= 30) {
                                Map<String, Object> lineResult = new HashMap<>();
                                lineResult.put("line", line);
                                lineResult.put("title", properties.get("title"));
                                lineResult.put("author", properties.get("author"));
                                results.add(lineResult);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return results;
    }

    // 获取知识图谱数据（用于可视化）
    public Map<String, Object> getGraphData(int limit) {
        limit = Math.min(limit, 500);

        List<GraphNode> nodes = graphNodeMapper.selectList(new QueryWrapper<GraphNode>().last("LIMIT " + limit));
        List<GraphEdge> edges = graphEdgeMapper.selectList(new QueryWrapper<GraphEdge>().last("LIMIT " + (limit * 2)));

        List<Map<String, Object>> nodeData = new ArrayList<>();
        Map<String, Integer> categoryMap = new LinkedHashMap<>();
        int categoryIndex = 0;

        for (GraphNode node : nodes) {
            if (!categoryMap.containsKey(node.getCategory())) {
                categoryMap.put(node.getCategory(), categoryIndex++);
            }

            Map<String, Object> nodeInfo = new HashMap<>();
            nodeInfo.put("name", node.getName());
            nodeInfo.put("category", categoryMap.get(node.getCategory()));
            nodeInfo.put("value", 1);
            nodeInfo.put("symbolSize", 20);
            nodeData.add(nodeInfo);
        }

        List<Map<String, Object>> edgeData = new ArrayList<>();
        for (GraphEdge edge : edges) {
            GraphNode source = graphNodeMapper.selectById(edge.getSourceId());
            GraphNode target = graphNodeMapper.selectById(edge.getTargetId());

            if (source != null && target != null) {
                Map<String, Object> edgeInfo = new HashMap<>();
                edgeInfo.put("source", source.getName());
                edgeInfo.put("target", target.getName());
                edgeData.add(edgeInfo);
            }
        }

        List<Map<String, Object>> categories = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("name", entry.getValue());
            cat.put("value", entry.getKey());
            categories.add(cat);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodeData);
        result.put("links", edgeData);
        result.put("categories", categories);

        return result;
    }

    // 获取与某个节点相关的诗词
    public List<Map<String, Object>> getRelatedPoems(String nodeName) {
        QueryWrapper<GraphNode> wrapper = new QueryWrapper<>();
        wrapper.eq("name", nodeName);
        GraphNode node = graphNodeMapper.selectOne(wrapper);

        if (node == null) {
            return new ArrayList<>();
        }

        Set<Long> poemIds = new HashSet<>();

        if ("诗人".equals(node.getCategory())) {
            List<GraphEdge> edges = graphEdgeMapper.findBySourceId(node.getId());
            for (GraphEdge edge : edges) {
                if ("创作".equals(edge.getRelation())) {
                    poemIds.add(edge.getTargetId());
                }
            }
        } else if ("朝代".equals(node.getCategory())) {
            List<GraphEdge> edges = graphEdgeMapper.findByTargetId(node.getId());
            for (GraphEdge edge : edges) {
                if ("属于朝代".equals(edge.getRelation())) {
                    poemIds.add(edge.getSourceId());
                }
            }
        } else if ("题材".equals(node.getCategory())) {
            List<GraphEdge> edges = graphEdgeMapper.findByTargetId(node.getId());
            for (GraphEdge edge : edges) {
                if ("属于题材".equals(edge.getRelation())) {
                    poemIds.add(edge.getSourceId());
                }
            }
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Long poemId : poemIds) {
            QueryWrapper<GraphNode> poemWrapper = new QueryWrapper<>();
            poemWrapper.eq("id", poemId).eq("category", "诗词");
            GraphNode poemNode = graphNodeMapper.selectOne(poemWrapper);

            if (poemNode != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> properties = mapper.readValue(poemNode.getProperties(), Map.class);
                    results.add(properties);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return results;
    }

    // 获取特定节点的局部知识图谱
    public Map<String, Object> getSubGraphByNode(String type, String name) {
        String category = mapTypeToCategory(type);
        if (category == null) {
            return new HashMap<>();
        }

        QueryWrapper<GraphNode> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name).eq("category", category);
        GraphNode node = graphNodeMapper.selectOne(wrapper);

        if (node == null) {
            return new HashMap<>();
        }

        return buildSubGraphFromNode(node);
    }

    // 根据类型映射到类别
    private String mapTypeToCategory(String type) {
        switch (type) {
            case "poet":
                return "诗人";
            case "dynasty":
                return "朝代";
            case "poem":
                return "诗词";
            case "tag":
                return "题材";
            default:
                return null;
        }
    }

    // 从单个节点构建局部知识图谱
    private Map<String, Object> buildSubGraphFromNode(GraphNode node) {
        Set<Long> relatedNodeIds = new HashSet<>();
        relatedNodeIds.add(node.getId());

        List<GraphEdge> relatedEdges = new ArrayList<>();

        // 查找与该节点相关的边
        List<GraphEdge> sourceEdges = graphEdgeMapper.findBySourceId(node.getId());
        List<GraphEdge> targetEdges = graphEdgeMapper.findByTargetId(node.getId());

        relatedEdges.addAll(sourceEdges);
        relatedEdges.addAll(targetEdges);

        // 收集相关节点ID
        for (GraphEdge edge : sourceEdges) {
            relatedNodeIds.add(edge.getTargetId());
        }
        for (GraphEdge edge : targetEdges) {
            relatedNodeIds.add(edge.getSourceId());
        }

        // 对于诗人节点，获取其所有诗词及关联的朝代和题材
        if ("诗人".equals(node.getCategory())) {
            for (GraphEdge edge : sourceEdges) {
                if ("创作".equals(edge.getRelation())) {
                    GraphNode poemNode = graphNodeMapper.selectById(edge.getTargetId());
                    if (poemNode != null) {
                        // 获取诗词关联的朝代和题材
                        List<GraphEdge> poemEdges = graphEdgeMapper.findBySourceId(poemNode.getId());
                        for (GraphEdge poemEdge : poemEdges) {
                            relatedNodeIds.add(poemEdge.getTargetId());
                            relatedEdges.add(poemEdge);
                        }
                    }
                }
            }
        }

        // 对于朝代节点，获取该朝代下所有诗人及诗词
        if ("朝代".equals(node.getCategory())) {
            for (GraphEdge edge : targetEdges) {
                if ("属于朝代".equals(edge.getRelation())) {
                    GraphNode poemNode = graphNodeMapper.selectById(edge.getSourceId());
                    if (poemNode != null) {
                        relatedNodeIds.add(poemNode.getId());
                        // 获取诗词关联的作者和题材
                        List<GraphEdge> poemTargetEdges = graphEdgeMapper.findByTargetId(poemNode.getId());
                        List<GraphEdge> poemSourceEdges = graphEdgeMapper.findBySourceId(poemNode.getId());
                        relatedEdges.addAll(poemTargetEdges);
                        relatedEdges.addAll(poemSourceEdges);
                        for (GraphEdge poemEdge : poemTargetEdges) {
                            relatedNodeIds.add(poemEdge.getSourceId());
                        }
                        for (GraphEdge poemEdge : poemSourceEdges) {
                            relatedNodeIds.add(poemEdge.getTargetId());
                        }
                    }
                }
            }
        }

        // 对于题材节点，获取该题材下所有诗词及关联的诗人和朝代
        if ("题材".equals(node.getCategory())) {
            for (GraphEdge edge : targetEdges) {
                if ("属于题材".equals(edge.getRelation())) {
                    GraphNode poemNode = graphNodeMapper.selectById(edge.getSourceId());
                    if (poemNode != null) {
                        relatedNodeIds.add(poemNode.getId());
                        // 获取诗词关联的作者和朝代
                        List<GraphEdge> poemTargetEdges = graphEdgeMapper.findByTargetId(poemNode.getId());
                        List<GraphEdge> poemSourceEdges = graphEdgeMapper.findBySourceId(poemNode.getId());
                        relatedEdges.addAll(poemTargetEdges);
                        relatedEdges.addAll(poemSourceEdges);
                        for (GraphEdge poemEdge : poemTargetEdges) {
                            relatedNodeIds.add(poemEdge.getSourceId());
                        }
                        for (GraphEdge poemEdge : poemSourceEdges) {
                            relatedNodeIds.add(poemEdge.getTargetId());
                        }
                    }
                }
            }
        }

        // 对于诗词节点，获取关联的诗人、朝代和题材
        if ("诗词".equals(node.getCategory())) {
            // 已经包含了所有关联节点
        }

        // 构建节点数据
        List<Map<String, Object>> nodeData = new ArrayList<>();
        Map<String, Integer> categoryMap = new LinkedHashMap<>();
        int categoryIndex = 0;
        Map<Long, String> nodeIdToNameMap = new HashMap<>();

        for (Long nodeId : relatedNodeIds) {
            GraphNode graphNode = graphNodeMapper.selectById(nodeId);
            if (graphNode != null) {
                if (!categoryMap.containsKey(graphNode.getCategory())) {
                    categoryMap.put(graphNode.getCategory(), categoryIndex++);
                }

                Map<String, Object> nodeInfo = new HashMap<>();
                nodeInfo.put("name", graphNode.getName());
                nodeInfo.put("category", categoryMap.get(graphNode.getCategory()));
                nodeInfo.put("value", 1);
                nodeInfo.put("symbolSize", "诗词".equals(graphNode.getCategory()) ? 15 : 25);
                nodeInfo.put("description", getNodeDescription(graphNode));
                nodeIdToNameMap.put(nodeId, graphNode.getName());
                nodeData.add(nodeInfo);
            }
        }

        // 构建边数据
        List<Map<String, Object>> edgeData = new ArrayList<>();
        for (GraphEdge edge : relatedEdges) {
            String sourceName = nodeIdToNameMap.get(edge.getSourceId());
            String targetName = nodeIdToNameMap.get(edge.getTargetId());

            if (sourceName != null && targetName != null) {
                Map<String, Object> edgeInfo = new HashMap<>();
                edgeInfo.put("source", sourceName);
                edgeInfo.put("target", targetName);
                edgeInfo.put("relation", edge.getRelation());
                edgeData.add(edgeInfo);
            }
        }

        // 构建分类数据
        List<Map<String, Object>> categories = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("name", entry.getValue());
            cat.put("value", entry.getKey());
            categories.add(cat);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodeData);
        result.put("links", edgeData);
        result.put("categories", categories);
        result.put("centerNode", node.getName());
        result.put("centerCategory", node.getCategory());

        return result;
    }

    // 获取节点描述
    private String getNodeDescription(GraphNode node) {
        if (node.getProperties() == null) {
            return "";
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> properties = mapper.readValue(node.getProperties(), Map.class);
            if ("诗人".equals(node.getCategory())) {
                String dynasty = (String) properties.getOrDefault("dynasty", "");
                return dynasty + "诗人";
            } else if ("朝代".equals(node.getCategory())) {
                return node.getName() + "时期";
            } else if ("诗词".equals(node.getCategory())) {
                String author = (String) properties.getOrDefault("author", "");
                String dynasty = (String) properties.getOrDefault("dynasty", "");
                return dynasty + " · " + author;
            } else if ("题材".equals(node.getCategory())) {
                return node.getName() + "题材";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // 获取所有节点分类
    public Map<String, Object> getAllNodeCategories() {
        Map<String, Object> result = new HashMap<>();
        result.put("categories", Arrays.asList("诗人", "朝代", "题材", "诗词"));
        return result;
    }

    // 根据分类获取节点名称列表
    public List<String> getNodeNamesByCategory(String category) {
        QueryWrapper<GraphNode> wrapper = new QueryWrapper<>();
        wrapper.eq("category", category).select("name").orderByAsc("name");
        List<GraphNode> nodes = graphNodeMapper.selectList(wrapper);

        List<String> names = new ArrayList<>();
        for (GraphNode node : nodes) {
            names.add(node.getName());
        }
        return names;
    }
}