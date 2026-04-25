package com.poemweb.service;

import com.poemweb.entity.Poem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class FeihualingService {
    @Autowired
    private PoemService poemService;

    // 飞花令 - 按关键字返回含该字的诗句
    public List<FeihualingLine> getFeihualingLines(String charInput) {
        List<FeihualingLine> lines = new ArrayList<>();
        List<Poem> poems = poemService.getAllPoems();

        for (Poem p : poems) {
            if (p.getContent() != null && p.getContent().contains(charInput)) {
                // 分割诗句
                String[] contentLines = p.getContent().replace("。", "。\n").replace("，", "，\n").replace("？", "？\n").replace("！", "！\n").split("\n");
                for (String line : contentLines) {
                    line = line.trim();
                    if (!line.isEmpty() && line.contains(charInput) && line.length() <= 30) {
                        FeihualingLine feihualingLine = new FeihualingLine();
                        feihualingLine.setLine(line);
                        feihualingLine.setTitle(p.getTitle());
                        feihualingLine.setAuthor(p.getAuthor());
                        lines.add(feihualingLine);
                    }
                }
            }
        }

        // 限制返回数量
        if (lines.size() > 30) {
            return lines.subList(0, 30);
        }
        return lines;
    }

    // 飞花令诗句对象
    public static class FeihualingLine {
        private String line;
        private String title;
        private String author;

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }
    }
}