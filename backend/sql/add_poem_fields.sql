-- 为 poem 表添加 author, dynasty, tag 实际字段
-- 这些字段之前是 @TableField(exist = false) 不存数据库的，现在改为实际存储

ALTER TABLE poem_web.poem ADD COLUMN author VARCHAR(50) DEFAULT NULL COMMENT '作者姓名';
ALTER TABLE poem_web.poem ADD COLUMN dynasty VARCHAR(20) DEFAULT NULL COMMENT '朝代';
ALTER TABLE poem_web.poem ADD COLUMN tag VARCHAR(50) DEFAULT NULL COMMENT '题材标签';

-- 从知识图谱关联表中回填已有数据
UPDATE poem_web.poem p
LEFT JOIN poem_web.graph_node author_node ON p.author_id = author_node.id
SET p.author = author_node.name
WHERE p.author IS NULL AND p.author_id IS NOT NULL;

UPDATE poem_web.poem p
LEFT JOIN poem_web.graph_node dynasty_node ON p.dynasty_id = dynasty_node.id
SET p.dynasty = dynasty_node.name
WHERE p.dynasty IS NULL AND p.dynasty_id IS NOT NULL;

UPDATE poem_web.poem p
LEFT JOIN poem_web.graph_node tag_node ON p.tag_id = tag_node.id
SET p.tag = tag_node.name
WHERE p.tag IS NULL AND p.tag_id IS NOT NULL;
