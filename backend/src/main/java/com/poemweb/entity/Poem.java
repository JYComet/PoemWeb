package com.poemweb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@TableName("poem")
@Table(name = "poem")
public class Poem {
    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private Long authorId;
    private Long dynastyId;
    private Long tagId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String annotation;

    @Column(columnDefinition = "TEXT")
    private String translation;

    @Column(columnDefinition = "TEXT")
    private String background;

    @Column(columnDefinition = "TEXT")
    private String emotion;

    @Column(columnDefinition = "TEXT")
    private String allusion;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段（供XML resultMap映射使用，不映射到数据库列）
    @Transient
    private String author;

    @Transient
    private String dynasty;

    @Transient
    private String tag;
}