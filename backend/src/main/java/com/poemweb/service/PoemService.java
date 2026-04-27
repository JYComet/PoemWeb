package com.poemweb.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PoemService {
    @Autowired
    private PoemMapper poemMapper;
    @Autowired
    private AuthorMapper authorMapper;
    @Autowired
    private DynastyMapper dynastyMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    @Lazy
    private AIPoemService aiPoemService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 通过标题搜索诗词（增强版：数据库无结果时调用AI）
    public Poem searchPoemByTitleWithAI(String title) {
        // 先从数据库查找
        Poem poem = getPoemByTitle(title);
        
        // 如果数据库有结果，直接返回
        if (poem != null) {
            return poem;
        }
        
        // 数据库无结果，调用AI检索
        if (!title.isEmpty()) {
            List<Poem> aiPoems = searchPoemsFromAI(title);
            if (!aiPoems.isEmpty()) {
                return aiPoems.get(0); // 返回第一条AI找到的诗词
            }
        }
        
        return null;
    }

    // 搜索诗词（增强版：数据库无结果时调用AI）
    public List<Poem> searchPoems(String keyword, String author, String dynasty, String tag) {
        List<Poem> poems = poemMapper.searchPoems(keyword, author, dynasty, tag);
        
        // 如果数据库有结果，直接返回
        if (!poems.isEmpty()) {
            return poems;
        }
        
        // 数据库无结果，尝试调用AI检索
        if (!keyword.isEmpty()) {
            return searchPoemsFromAI(keyword);
        }
        
        return poems;
    }

    // 从AI检索古诗词
    private List<Poem> searchPoemsFromAI(String keyword) {
        try {
            String jsonResult = aiPoemService.searchPoemByAI(keyword);
            
            JsonNode rootNode = objectMapper.readTree(jsonResult);
            List<Poem> aiPoems = new ArrayList<>();
            
            if (rootNode.isArray()) {
                for (JsonNode poemNode : rootNode) {
                    Poem poem = new Poem();
                    poem.setTitle(poemNode.has("title") ? poemNode.get("title").asText() : "");
                    poem.setAuthor(poemNode.has("author") ? poemNode.get("author").asText() : "未知");
                    poem.setDynasty(poemNode.has("dynasty") ? poemNode.get("dynasty").asText() : "未知");
                    poem.setContent(poemNode.has("content") ? poemNode.get("content").asText() : "");
                    poem.setAnnotation(poemNode.has("annotation") ? poemNode.get("annotation").asText() : null);
                    poem.setTranslation(poemNode.has("translation") ? poemNode.get("translation").asText() : null);
                    poem.setBackground(poemNode.has("background") ? poemNode.get("background").asText() : null);
                    poem.setEmotion(poemNode.has("emotion") ? poemNode.get("emotion").asText() : null);
                    poem.setAllusion(poemNode.has("allusion") ? poemNode.get("allusion").asText() : null);
                    poem.setTag(poemNode.has("tag") ? poemNode.get("tag").asText() : "其他");
                    poem.setDataSource(1); // AI生成
                    poem.setCreateTime(LocalDateTime.now());
                    poem.setUpdateTime(LocalDateTime.now());
                    
                    // 保存到数据库
                    savePoem(poem);
                    aiPoems.add(poem);
                }
            }
            
            return aiPoems;
        } catch (Exception e) {
            System.err.println("AI检索古诗词失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // 根据标题获取诗词详情
    public Poem getPoemByTitle(String title) {
        return poemMapper.getPoemByTitle(title);
    }

    // 获取所有诗词
    public List<Poem> getAllPoems() {
        return poemMapper.getAllPoems();
    }

    // 随机获取一首诗词
    public Poem getRandomPoem() {
        List<Poem> poems = getAllPoems();
        if (poems.isEmpty()) {
            return null;
        }
        int randomIndex = (int) (Math.random() * poems.size());
        return poems.get(randomIndex);
    }

    // 根据作者ID获取诗词
    public List<Poem> getPoemsByAuthorId(Long authorId) {
        return poemMapper.getPoemsByAuthorId(authorId);
    }

    // 根据朝代ID获取诗词
    public List<Poem> getPoemsByDynastyId(Long dynastyId) {
        return poemMapper.getPoemsByDynastyId(dynastyId);
    }

    // 根据题材ID获取诗词
    public List<Poem> getPoemsByTagId(Long tagId) {
        return poemMapper.getPoemsByTagId(tagId);
    }

    // 保存诗词
    public Poem savePoem(Poem poem) {
        // 先保存关联实体
        Author author = getOrCreateAuthor(poem.getAuthor());
        Dynasty dynasty = getOrCreateDynasty(poem.getDynasty());
        Tag tag = getOrCreateTag(poem.getTag());

        // 设置关联ID
        poem.setAuthorId(author.getId());
        poem.setDynastyId(dynasty.getId());
        poem.setTagId(tag.getId());

        // 保存诗词
        poemMapper.insert(poem);
        return poem;
    }

    // 获取或创建作者
    private Author getOrCreateAuthor(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        QueryWrapper<Author> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        Author author = authorMapper.selectOne(wrapper);
        if (author == null) {
            author = new Author();
            author.setName(name);
            authorMapper.insert(author);
        }
        return author;
    }

    // 获取或创建朝代
    private Dynasty getOrCreateDynasty(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        QueryWrapper<Dynasty> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        Dynasty dynasty = dynastyMapper.selectOne(wrapper);
        if (dynasty == null) {
            dynasty = new Dynasty();
            dynasty.setName(name);
            dynastyMapper.insert(dynasty);
        }
        return dynasty;
    }

    // 获取或创建题材
    private Tag getOrCreateTag(String name) {
        if (name == null || name.isEmpty()) {
            name = "其他";
        }
        QueryWrapper<Tag> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        Tag tag = tagMapper.selectOne(wrapper);
        if (tag == null) {
            tag = new Tag();
            tag.setName(name);
            tagMapper.insert(tag);
        }
        return tag;
    }
}