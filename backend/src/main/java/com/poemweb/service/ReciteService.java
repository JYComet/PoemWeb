package com.poemweb.service;

import com.poemweb.entity.Poem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ReciteService {
    @Autowired
    private PoemService poemService;

    // 随机返回一首诗词（用于背诵）
    public Poem getRandomPoem() {
        return poemService.getRandomPoem();
    }

    // 返回带空白的诗词（用于默写）
    public Poem getDictationPoem() {
        Poem poem = poemService.getRandomPoem();
        if (poem == null) {
            return null;
        }

        String content = poem.getContent();
        if (content == null || content.isEmpty()) {
            return poem;
        }

        // 随机替换部分字为空白
        char[] chars = content.toCharArray();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c != '。' && c != '，' && c != '、' && c != '；' && c != '：' && c != '？' && c != '！' && c != '\n') {
                indices.add(i);
            }
        }

        int blanks = Math.min(5, indices.size());
        if (!indices.isEmpty()) {
            Random random = new Random();
            for (int i = 0; i < blanks; i++) {
                int index = random.nextInt(indices.size());
                int charIndex = indices.get(index);
                chars[charIndex] = '□';
                indices.remove(index);
            }
        }

        poem.setContent(new String(chars));
        return poem;
    }
}