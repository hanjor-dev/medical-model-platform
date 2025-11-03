-- 医学影像模型管理平台数据库初始化脚本
-- 创建时间: 2025-10-31

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS medical_model_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE medical_model_platform;

-- 此处可以添加表结构初始化SQL
-- 或者让应用启动时自动创建

-- 示例: 创建用户表 (如果需要)
-- CREATE TABLE IF NOT EXISTS users (
--   id BIGINT NOT NULL PRIMARY KEY,
--   username VARCHAR(50) NOT NULL UNIQUE,
--   ...
-- );

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 输出初始化完成信息
SELECT '数据库初始化完成' AS status;

