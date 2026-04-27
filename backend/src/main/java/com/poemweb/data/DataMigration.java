package com.poemweb.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poemweb.entity.Author;
import com.poemweb.entity.Dynasty;
import com.poemweb.entity.Poem;
import com.poemweb.entity.Tag;
import com.poemweb.mapper.AuthorMapper;
import com.poemweb.mapper.DynastyMapper;
import com.poemweb.mapper.PoemMapper;
import com.poemweb.mapper.TagMapper;
import com.poemweb.service.GraphDbService;
import com.poemweb.service.TrajectoryService;
import com.poemweb.service.PoemDetailEnhancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(2)
public class DataMigration implements CommandLineRunner {

    @Autowired
    private PoemMapper poemMapper;

    @Autowired
    private AuthorMapper authorMapper;

    @Autowired
    private DynastyMapper dynastyMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private GraphDbService graphDbService;

    @Autowired
    private TrajectoryService trajectoryService;

    @Autowired
    private PoemDetailEnhancer poemDetailEnhancer;

    private Map<String, Long> authorCache = new HashMap<>();
    private Map<String, Long> dynastyCache = new HashMap<>();
    private Map<String, Long> tagCache = new HashMap<>();

    @Override
    public void run(String... args) throws Exception {
        migratePoemData();
    }

    private void migratePoemData() {
        System.out.println("开始导入诗词数据...");

        try {
            File file = new File("../data/poems.json");
            if (!file.exists()) {
                // 尝试另一个路径
                file = new File("../../data/poems.json");
                if (!file.exists()) {
                    System.out.println("未找到数据文件: data/poems.json");
                    return;
                }
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(file);

            int count = 0;
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    try {
                        savePoem(node);
                        count++;
                        if (count % 100 == 0) {
                            System.out.println("已导入 " + count + " 首诗词");
                        }
                    } catch (Exception e) {
                        System.err.println("导入诗词失败: " + node.get("title").asText() + " - " + e.getMessage());
                    }
                }
            }

            System.out.println("数据导入完成，共导入 " + count + " 首诗词");

            // 构建知识图谱
            System.out.println("开始构建知识图谱...");
            graphDbService.buildKnowledgeGraph();

            // 初始化诗人行迹数据
            System.out.println("开始初始化诗人行迹数据...");
            trajectoryService.initTrajectoryFromGraph();

            // 丰富诗词详细信息
            System.out.println("开始丰富诗词详细信息...");
            poemDetailEnhancer.enhancePoemDetails();

        } catch (Exception e) {
            System.err.println("数据导入失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void savePoem(JsonNode node) {
        String title = node.get("title").asText();
        String authorName = node.get("author").asText();
        String dynastyName = node.get("dynasty").asText();
        String tagName = node.has("tag") ? node.get("tag").asText() : "其他";
        String content = node.get("content").asText();

        // 获取或创建作者
        Long authorId = getOrCreateAuthor(authorName);

        // 获取或创建朝代
        Long dynastyId = getOrCreateDynasty(dynastyName);

        // 获取或创建题材
        Long tagId = getOrCreateTag(tagName);

        // 创建诗词
        Poem poem = new Poem();
        poem.setTitle(title);
        poem.setAuthorId(authorId);
        poem.setDynastyId(dynastyId);
        poem.setTagId(tagId);
        poem.setContent(content);
        poem.setCreateTime(LocalDateTime.now());
        poem.setUpdateTime(LocalDateTime.now());

        // 处理可选字段
        if (node.has("annotation") && !node.get("annotation").asText().isEmpty()) {
            poem.setAnnotation(node.get("annotation").asText());
        }
        if (node.has("translation") && !node.get("translation").asText().isEmpty()) {
            poem.setTranslation(node.get("translation").asText());
        }
        if (node.has("background") && !node.get("background").asText().isEmpty()) {
            poem.setBackground(node.get("background").asText());
        }
        if (node.has("emotion") && !node.get("emotion").asText().isEmpty()) {
            poem.setEmotion(node.get("emotion").asText());
        }
        if (node.has("allusion") && !node.get("allusion").asText().isEmpty()) {
            poem.setAllusion(node.get("allusion").asText());
        }

        poemMapper.insert(poem);
    }

    private Long getOrCreateAuthor(String name) {
        if (authorCache.containsKey(name)) {
            return authorCache.get(name);
        }

        // 查询是否已存在
        Author existing = authorMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Author>().eq("name", name)
        );

        if (existing != null) {
            authorCache.put(name, existing.getId());
            return existing.getId();
        }

        // 创建新作者
        Author author = new Author();
        author.setName(name);
        author.setCreateTime(LocalDateTime.now());
        author.setUpdateTime(LocalDateTime.now());
        authorMapper.insert(author);

        authorCache.put(name, author.getId());
        return author.getId();
    }

    private Long getOrCreateDynasty(String name) {
        if (dynastyCache.containsKey(name)) {
            return dynastyCache.get(name);
        }

        // 查询是否已存在
        Dynasty existing = dynastyMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Dynasty>().eq("name", name)
        );

        if (existing != null) {
            dynastyCache.put(name, existing.getId());
            return existing.getId();
        }

        // 创建新朝代
        Dynasty dynasty = new Dynasty();
        dynasty.setName(name);
        dynasty.setCreateTime(LocalDateTime.now());
        dynasty.setUpdateTime(LocalDateTime.now());
        dynastyMapper.insert(dynasty);

        dynastyCache.put(name, dynasty.getId());
        return dynasty.getId();
    }

    private Long getOrCreateTag(String name) {
        if (tagCache.containsKey(name)) {
            return tagCache.get(name);
        }

        // 查询是否已存在
        Tag existing = tagMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Tag>().eq("name", name)
        );

        if (existing != null) {
            tagCache.put(name, existing.getId());
            return existing.getId();
        }

        // 创建新题材
        Tag tag = new Tag();
        tag.setName(name);
        tag.setCreateTime(LocalDateTime.now());
        tag.setUpdateTime(LocalDateTime.now());
        tagMapper.insert(tag);

        tagCache.put(name, tag.getId());
        return tag.getId();
    }
}