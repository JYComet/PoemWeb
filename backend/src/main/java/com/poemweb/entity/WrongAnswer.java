package com.poemweb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@TableName("wrong_answer")
@Table(name = "wrong_answer")
public class WrongAnswer {
    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String userAnswer;

    @Column(nullable = false)
    private String knowledgePoint;

    @Column(nullable = false)
    private String relatedPoem;

    private Integer wrongCount = 1; // 错误次数

    private LocalDateTime lastWrongTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
