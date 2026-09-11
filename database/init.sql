SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `scaffolding_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `scaffolding_db`;

-- 文件信息表
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT '0' COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `file_extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `upload_user_id` bigint(20) DEFAULT NULL COMMENT '上传人ID',
  `upload_user_name` varchar(50) DEFAULT NULL COMMENT '上传人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

-- 工作管理表
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_name` varchar(100) NOT NULL COMMENT '工作名称',
  `work_content` text COMMENT '工作内容',
  `work_status` varchar(20) DEFAULT 'pending' COMMENT '工作状态（pending-待处理，in_progress-进行中，completed-已完成，cancelled-已取消）',
  `work_time` datetime DEFAULT NULL COMMENT '工作时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `priority` varchar(20) DEFAULT 'normal' COMMENT '优先级（low-低，normal-普通，high-高，urgent-紧急）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_work_status` (`work_status`),
  KEY `idx_work_time` (`work_time`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作管理表';

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（账号）',
  `password` varchar(100) NOT NULL COMMENT '密码（不加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认admin账号
INSERT INTO `user` (`username`, `password`, `nickname`) VALUES ('admin', '123456', '管理员');

SET FOREIGN_KEY_CHECKS = 1;
