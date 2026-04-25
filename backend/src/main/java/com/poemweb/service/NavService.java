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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NavService {
    @Autowired
    private AuthorMapper authorMapper;
    @Autowired
    private DynastyMapper dynastyMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private PoemMapper poemMapper;

    // 诗人分类导航
    public List<AuthorNavItem> getAuthorNav() {
        List<Author> authors = authorMapper.selectList(null);
        List<AuthorNavItem> navItems = new ArrayList<>();

        for (Author author : authors) {
            // 统计该作者的诗词数量
            List<Poem> poems = poemMapper.getPoemsByAuthorId(author.getId());
            int count = poems.size();

            // 获取该作者的朝代
            String dynasty = "";
            if (!poems.isEmpty()) {
                dynasty = poems.get(0).getDynasty();
            }

            AuthorNavItem item = new AuthorNavItem();
            item.setName(author.getName());
            item.setDynasty(dynasty);
            item.setCount(count);
            navItems.add(item);
        }

        // 按诗词数量排序
        navItems.sort((a, b) -> b.getCount() - a.getCount());
        return navItems;
    }

    // 题材分类导航
    public List<TagNavItem> getTagNav() {
        List<Tag> tags = tagMapper.selectList(null);
        List<TagNavItem> navItems = new ArrayList<>();

        for (Tag tag : tags) {
            // 统计该题材的诗词数量
            List<Poem> poems = poemMapper.getPoemsByTagId(tag.getId());
            int count = poems.size();

            TagNavItem item = new TagNavItem();
            item.setName(tag.getName());
            item.setCount(count);
            navItems.add(item);
        }

        // 按诗词数量排序
        navItems.sort((a, b) -> b.getCount() - a.getCount());
        return navItems;
    }

    // 朝代分类导航
    public List<DynastyNavItem> getDynastyNav() {
        List<Dynasty> dynasties = dynastyMapper.selectList(null);
        List<DynastyNavItem> navItems = new ArrayList<>();

        for (Dynasty dynasty : dynasties) {
            // 统计该朝代的诗词数量
            List<Poem> poems = poemMapper.getPoemsByDynastyId(dynasty.getId());
            int count = poems.size();

            DynastyNavItem item = new DynastyNavItem();
            item.setName(dynasty.getName());
            item.setCount(count);
            navItems.add(item);
        }

        // 按诗词数量排序
        navItems.sort((a, b) -> b.getCount() - a.getCount());
        return navItems;
    }

    // 诗人导航项
    public static class AuthorNavItem {
        private String name;
        private String dynasty;
        private int count;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDynasty() {
            return dynasty;
        }

        public void setDynasty(String dynasty) {
            this.dynasty = dynasty;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    // 题材导航项
    public static class TagNavItem {
        private String name;
        private int count;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    // 朝代导航项
    public static class DynastyNavItem {
        private String name;
        private int count;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}