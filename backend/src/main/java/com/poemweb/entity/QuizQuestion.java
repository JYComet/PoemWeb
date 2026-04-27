package com.poemweb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@TableName("quiz_question")
@Table(name = "quiz_question")
public class QuizQuestion {
    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = false)
    private String questionType; // 选择题、填空题、判断题

    @Column(columnDefinition = "TEXT")
    private String options; // JSON格式，存储选项

    @Column(nullable = false)
    private String knowledgePoint; // 知识点

    @Column(nullable = false)
    private String relatedPoem; // 相关诗词

    private Integer difficulty; // 1-5难度等级

    private LocalDateTime createTime;
}
