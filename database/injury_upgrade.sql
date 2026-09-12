-- =====================================================================
-- 工伤上报材料包 - 已存在环境升级脚本（MySQL 8.0，幂等，可重复执行）
-- 执行方式：mysql -uroot -p scaffolding_db < database/injury_upgrade.sql
-- =====================================================================
SET NAMES utf8mb4;
USE `scaffolding_db`;

-- 1. user 表加列（若缺失）
SET @ddl := (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'role') = 0,
  'ALTER TABLE `user` ADD COLUMN `role` varchar(20) DEFAULT ''admin'' COMMENT ''角色'' AFTER `nickname`',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @ddl := (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'phone') = 0,
  'ALTER TABLE `user` ADD COLUMN `phone` varchar(20) DEFAULT NULL COMMENT ''联系电话'' AFTER `role`',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @ddl := (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'project_id') = 0,
  'ALTER TABLE `user` ADD COLUMN `project_id` bigint(20) DEFAULT NULL COMMENT ''所属项目ID'' AFTER `phone`',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

SET @ddl := (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'company_name') = 0,
  'ALTER TABLE `user` ADD COLUMN `company_name` varchar(100) DEFAULT NULL COMMENT ''劳务公司名称'' AFTER `project_id`',
  'SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

-- 2. 业务表（CREATE TABLE IF NOT EXISTS）
CREATE TABLE IF NOT EXISTS `project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_name` varchar(100) NOT NULL COMMENT '项目名称',
  `enterprise_name` varchar(100) DEFAULT NULL COMMENT '用工企业名称',
  `labor_company` varchar(100) DEFAULT NULL COMMENT '合作劳务公司名称',
  `address` varchar(255) DEFAULT NULL COMMENT '项目地址',
  `status` varchar(20) DEFAULT 'active',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

CREATE TABLE IF NOT EXISTS `worker` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `worker_name` varchar(50) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `id_card` varchar(30) DEFAULT NULL,
  `labor_company` varchar(100) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'active',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_labor_company` (`labor_company`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工人表';

CREATE TABLE IF NOT EXISTS `schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) NOT NULL,
  `worker_id` bigint(20) NOT NULL,
  `post_name` varchar(50) DEFAULT NULL,
  `shift` varchar(20) DEFAULT 'day',
  `schedule_date` date DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `schedule_status` varchar(20) DEFAULT 'normal',
  `injury_report_id` bigint(20) DEFAULT NULL,
  `stop_start_date` date DEFAULT NULL,
  `resume_date` date DEFAULT NULL,
  `conclusion` varchar(30) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_project_worker` (`project_id`,`worker_id`),
  KEY `idx_schedule_date` (`schedule_date`),
  KEY `idx_injury_report` (`injury_report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班档案表';

CREATE TABLE IF NOT EXISTS `injury_report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `report_no` varchar(40) NOT NULL,
  `project_id` bigint(20) NOT NULL,
  `schedule_id` bigint(20) DEFAULT NULL,
  `worker_id` bigint(20) NOT NULL,
  `worker_name` varchar(50) DEFAULT NULL,
  `post_name` varchar(50) DEFAULT NULL,
  `shift_name` varchar(20) DEFAULT NULL,
  `injury_time` datetime NOT NULL,
  `injury_location` varchar(255) NOT NULL,
  `injury_desc` text,
  `injury_type` varchar(20) DEFAULT 'outpatient',
  `hospital` varchar(150) DEFAULT NULL,
  `hospital_time` datetime DEFAULT NULL,
  `hospital_diagnosis` varchar(500) DEFAULT NULL,
  `reporter_id` bigint(20) DEFAULT NULL,
  `reporter_name` varchar(50) DEFAULT NULL,
  `report_time` datetime DEFAULT NULL,
  `report_status` varchar(20) DEFAULT 'draft',
  `conclusion` varchar(20) DEFAULT NULL,
  `conclusion_remark` varchar(500) DEFAULT NULL,
  `stop_start_date` date DEFAULT NULL,
  `expected_resume_date` date DEFAULT NULL,
  `actual_resume_date` date DEFAULT NULL,
  `concluded_by` varchar(50) DEFAULT NULL,
  `concluded_time` datetime DEFAULT NULL,
  `material_progress` int DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_no` (`report_no`),
  KEY `idx_project` (`project_id`),
  KEY `idx_worker` (`worker_id`),
  KEY `idx_schedule` (`schedule_id`),
  KEY `idx_status` (`report_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工伤上报表';

CREATE TABLE IF NOT EXISTS `injury_witness` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `report_id` bigint(20) NOT NULL,
  `witness_name` varchar(50) NOT NULL,
  `witness_phone` varchar(20) DEFAULT NULL,
  `witness_type` varchar(20) DEFAULT 'coworker',
  `statement` varchar(1000) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_report` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工伤见证人表';

CREATE TABLE IF NOT EXISTS `injury_material` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `report_id` bigint(20) NOT NULL,
  `material_type` varchar(40) NOT NULL,
  `material_name` varchar(100) NOT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `original_name` varchar(255) DEFAULT NULL,
  `file_path` varchar(500) DEFAULT NULL,
  `file_url` varchar(500) DEFAULT NULL,
  `file_size` bigint(20) DEFAULT 0,
  `file_ext` varchar(20) DEFAULT NULL,
  `owner_role` varchar(20) DEFAULT 'supervisor',
  `upload_user_id` bigint(20) DEFAULT NULL,
  `upload_user_name` varchar(50) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_report_type` (`report_id`,`material_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工伤证明材料表';

CREATE TABLE IF NOT EXISTS `injury_insurance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `report_id` bigint(20) NOT NULL,
  `insured_name` varchar(50) DEFAULT NULL,
  `insurance_company` varchar(100) DEFAULT NULL,
  `policy_no` varchar(80) DEFAULT NULL,
  `coverage_start_date` date DEFAULT NULL,
  `coverage_end_date` date DEFAULT NULL,
  `claim_status` varchar(20) DEFAULT 'not_filed',
  `claim_no` varchar(80) DEFAULT NULL,
  `claim_amount` decimal(12,2) DEFAULT NULL,
  `contact_name` varchar(50) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工伤保险资料表';

CREATE TABLE IF NOT EXISTS `schedule_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `schedule_id` bigint(20) NOT NULL,
  `report_id` bigint(20) DEFAULT NULL,
  `event_type` varchar(30) NOT NULL,
  `event_content` varchar(1000) DEFAULT NULL,
  `operator_id` bigint(20) DEFAULT NULL,
  `operator_name` varchar(50) DEFAULT NULL,
  `event_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_evt_schedule` (`schedule_id`),
  KEY `idx_evt_report` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班档案事件表';

-- 3. 演示账号（重复执行不报错）
-- 工伤模块演示账号（已存在同名账号则跳过；MySQL/H2 均支持 DUAL 写法）
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `phone`, `project_id`, `company_name`)
SELECT 'supervisor1','123456','刘主管','supervisor','13900000001',1,NULL FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `user` u WHERE u.username='supervisor1');
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `phone`, `project_id`, `company_name`)
SELECT 'labor1','123456','安达劳务-陈经办','labor','13900000002',NULL,'安达劳务派遣有限公司' FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `user` u WHERE u.username='labor1');
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `phone`, `project_id`, `company_name`)
SELECT 'enterprise1','123456','华东建工-安全科','enterprise','13900000003',1,NULL FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `user` u WHERE u.username='enterprise1');
