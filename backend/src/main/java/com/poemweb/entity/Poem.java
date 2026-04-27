package com.poemweb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0 COMMENT '0:数据库自带, 1:AI生成'")
    private Integer dataSource = 0;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 数据库字段：作者、朝代、题材（由知识图谱关联或AI补充）
    private String author;

    private String dynasty;

    private String tag;
}