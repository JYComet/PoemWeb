-- 为诗词表新增数据来源字段
ALTER TABLE poem_web.poem ADD COLUMN data_source TINYINT NOT NULL DEFAULT 0 COMMENT '0:数据库自带, 1:AI生成' AFTER allusion;
