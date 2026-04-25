package com.poemweb.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.poemweb.entity.Author;
import com.poemweb.entity.Dynasty;
import com.poemweb.entity.Poem;
import com.poemweb.entity.Tag;
import com.poemweb.mapper.AuthorMapper;
import com.poemweb.mapper.DynastyMapper;
import com.poemweb.mapper.PoemMapper;
import com.poemweb.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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

    // 搜索诗词
    public List<Poem> searchPoems(String keyword, String author, String dynasty, String tag) {
        return poemMapper.searchPoems(keyword, author, dynasty, tag);
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