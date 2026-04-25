package com.poemweb.service;

import com.poemweb.entity.Poem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Service
public class QAService {
    @Autowired
    private PoemService poemService;

    @Autowired
    private GraphDbService graphDbService;

    @Autowired
    private AIPoemService aiPoemService;

    // 智能问答（基于知识图谱+AI大模型）
    public Object intelligentQA(String question) {
        // 使用AI大模型结合知识图谱进行问答
        return aiPoemService.answerQuestion(question);
    }

    // AI生成诗词描述
    public String aiDescribePoem(String title, String author, String dynasty, String content) {
        return aiPoemService.describePoem(title, author, dynasty, content);
    }
}