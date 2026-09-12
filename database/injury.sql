-- =====================================================================
-- 蓝领招聘排班平台 - 工伤上报材料包  DDL / 演示数据
-- 依赖 init.sql 中已有的 user 表；可重复执行（先 DROP 再 CREATE）
-- 字符集：utf8mb4
-- =====================================================================
SET NAMES utf8mb4;
USE `scaffolding_db`;

-- ---------------------------------------------------------------------
-- 1. user 表扩展：角色 + 所属项目（已存在库请改用 injury_upgrade.sql 做幂等升级）
--    全新初始化直接使用 init.sql（已包含下列字段）
-- ---------------------------------------------------------------------
SET @ddl := (SELECT IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user' AND COLUMN_NAME='role')=0,
  'ALTER TABLE `user` ADD COLUMN `role` varchar(20) DEFAULT ''admin'' COMMENT ''角色（admin/supervisor/labor/enterprise）'' AFTER `nickname`','SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;
SET @ddl := (SELECT IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user' AND COLUMN_NAME='phone')=0,
  'ALTER TABLE `user` ADD COLUMN `phone` varchar(20) DEFAULT NULL COMMENT ''联系电话'' AFTER `role`','SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;
SET @ddl := (SELECT IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user' AND COLUMN_NAME='project_id')=0,
  'ALTER TABLE `user` ADD COLUMN `project_id` bigint(20) DEFAULT NULL COMMENT ''所属项目ID'' AFTER `phone`','SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;
SET @ddl := (SELECT IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user' AND COLUMN_NAME='company_name')=0,
  'ALTER TABLE `user` ADD COLUMN `company_name` varchar(100) DEFAULT NULL COMMENT ''劳务公司名称'' AFTER `project_id`','SELECT 1'));
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

-- ---------------------------------------------------------------------
-- 2. 项目表（企业发包、劳务派人、主管现场管理的最小单位）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_name` varchar(100) NOT NULL COMMENT '项目名称',
  `enterprise_name` varchar(100) DEFAULT NULL COMMENT '用工企业名称',
  `labor_company` varchar(100) DEFAULT NULL COMMENT '合作劳务公司名称',
  `address` varchar(255) DEFAULT NULL COMMENT '项目地址',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态（active-进行中，closed-已结束）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除（0-否，1-是）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- ---------------------------------------------------------------------
-- 3. 工人表（劳务公司名下的蓝领工人）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `worker`;
CREATE TABLE `worker` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `worker_name` varchar(50) NOT NULL COMMENT '姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `id_card` varchar(30) DEFAULT NULL COMMENT '身份证号',
  `labor_company` varchar(100) DEFAULT NULL COMMENT '所属劳务公司',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态（active-在职，disabled-停用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除（0-否，1-是）',
  PRIMARY KEY (`id`),
  KEY `idx_labor_company` (`labor_company`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工人表';

-- ---------------------------------------------------------------------
-- 4. 排班档案表（工人 × 项目 × 班次；工伤结论回写到此表）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `worker_id` bigint(20) NOT NULL COMMENT '工人ID',
  `post_name` varchar(50) DEFAULT NULL COMMENT '岗位（如：架子工/电焊工/普工）',
  `shift` varchar(20) DEFAULT 'day' COMMENT '班次（day-白班，night-夜班，middle-中班）',
  `schedule_date` date DEFAULT NULL COMMENT '排班日期',
  `start_time` datetime DEFAULT NULL COMMENT '实际上班时间',
  `end_time` datetime DEFAULT NULL COMMENT '实际下班时间',
  `schedule_status` varchar(20) DEFAULT 'normal' COMMENT '档案状态（normal-正常排班，injury_stop-工伤停工，resumed-已复工）',
  `injury_report_id` bigint(20) DEFAULT NULL COMMENT '关联工伤上报单ID',
  `stop_start_date` date DEFAULT NULL COMMENT '停工开始日期（工伤结论回写）',
  `resume_date` date DEFAULT NULL COMMENT '复工日期（工伤结论回写）',
  `conclusion` varchar(30) DEFAULT NULL COMMENT '最新结论（pending-待结论，stop-停工，resume-复工）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除（0-否，1-是）',
  PRIMARY KEY (`id`),
  KEY `idx_project_worker` (`project_id`, `worker_id`),
  KEY `idx_schedule_date` (`schedule_date`),
  KEY `idx_injury_report` (`injury_report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班档案表';

-- ---------------------------------------------------------------------
-- 5. 工伤上报表（主管手机端填报的主单）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `injury_report`;
CREATE TABLE `injury_report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_no` varchar(40) NOT NULL COMMENT '上报单号（GS+年月日+4位序列）',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `schedule_id` bigint(20) DEFAULT NULL COMMENT '关联排班档案ID（岗位班次来源）',
  `worker_id` bigint(20) NOT NULL COMMENT '受伤工人ID',
  `worker_name` varchar(50) DEFAULT NULL COMMENT '受伤工人姓名（冗余）',
  `post_name` varchar(50) DEFAULT NULL COMMENT '岗位（来自排班档案）',
  `shift_name` varchar(20) DEFAULT NULL COMMENT '班次（day/night/middle）',
  `injury_time` datetime NOT NULL COMMENT '受伤时间',
  `injury_location` varchar(255) NOT NULL COMMENT '受伤地点',
  `injury_desc` text COMMENT '受伤经过与伤情描述',
  `injury_type` varchar(20) DEFAULT 'outpatient' COMMENT '伤情类型（outpatient-门诊/轻伤，hospitalized-住院，disability-疑似伤残，death-工亡）',
  `hospital` varchar(150) DEFAULT NULL COMMENT '送医医院',
  `hospital_time` datetime DEFAULT NULL COMMENT '送医时间',
  `hospital_diagnosis` varchar(500) DEFAULT NULL COMMENT '医院诊断（劳务/主管补充）',
  `reporter_id` bigint(20) DEFAULT NULL COMMENT '上报主管用户ID',
  `reporter_name` varchar(50) DEFAULT NULL COMMENT '上报主管姓名',
  `report_time` datetime DEFAULT NULL COMMENT '上报时间',
  `report_status` varchar(20) DEFAULT 'draft' COMMENT '单据状态（draft-待提交，reported-已上报，submitted-材料已提交，concluded-已结论）',
  `conclusion` varchar(20) DEFAULT NULL COMMENT '复工/停工结论（stop-停工，resume-复工）',
  `conclusion_remark` varchar(500) DEFAULT NULL COMMENT '结论说明（医嘱/复工条件）',
  `stop_start_date` date DEFAULT NULL COMMENT '停工开始日期',
  `expected_resume_date` date DEFAULT NULL COMMENT '预计复工日期',
  `actual_resume_date` date DEFAULT NULL COMMENT '实际复工日期',
  `concluded_by` varchar(50) DEFAULT NULL COMMENT '结论确认人',
  `concluded_time` datetime DEFAULT NULL COMMENT '结论确认时间',
  `material_progress` int DEFAULT 0 COMMENT '材料完成度（0-100，提交材料时重算）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除（0-否，1-是）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_no` (`report_no`),
  KEY `idx_project` (`project_id`),
  KEY `idx_worker` (`worker_id`),
  KEY `idx_schedule` (`schedule_id`),
  KEY `idx_status` (`report_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工伤上报表';

-- ---------------------------------------------------------------------
-- 6. 工伤见证人表（一份上报可多名见证人）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `injury_witness`;
CREATE TABLE `injury_witness` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_id` bigint(20) NOT NULL COMMENT '工伤上报单ID',
  `witness_name` varchar(50) NOT NULL COMMENT '见证人姓名',
  `witness_phone` varchar(20) DEFAULT NULL COMMENT '见证人联系电话',
  `witness_type` varchar(20) DEFAULT 'coworker' COMMENT '身份（coworker-同班组工人，manager-现场管理人员，other-其他）',
  `statement` varchar(1000) DEFAULT NULL COMMENT '见证情况说明',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除（0-否，1-是）',
  PRIMARY KEY (`id`),
  KEY `idx_report` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工伤见证人表';

-- ---------------------------------------------------------------------
-- 7. 工伤证明材料表（按材料类型维护，平台据此提示缺件）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `injury_material`;
CREATE TABLE `injury_material` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_id` bigint(20) NOT NULL COMMENT '工伤上报单ID',
  `material_type` varchar(40) NOT NULL COMMENT '材料类型编码（site_photo/witness_statement/accident_report/medical_diagnosis/medical_receipt/hospital_record/labor_contract/insurance_cert/id_card/payment_voucher/disability_appraisal/death_cert 等）',
  `material_name` varchar(100) NOT NULL COMMENT '材料名称',
  `file_name` varchar(255) DEFAULT NULL COMMENT '存储文件名',
  `original_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_path` varchar(500) DEFAULT NULL COMMENT '相对存储路径',
  `file_url` varchar(500) DEFAULT NULL COMMENT '访问URL',
  `file_size` bigint(20) DEFAULT 0 COMMENT '文件大小（字节）',
  `file_ext` varchar(20) DEFAULT NULL COMMENT '扩展名',
  `owner_role` varchar(20) DEFAULT 'supervisor' COMMENT '材料归属（supervisor-现场证明，labor-保险资料，common-医疗票据）',
  `upload_user_id` bigint(20) DEFAULT NULL COMMENT '上传人ID',
  `upload_user_name` varchar(50) DEFAULT NULL COMMENT '上传人姓名',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除（0-否，1-是）',
  PRIMARY KEY (`id`),
  KEY `idx_report_type` (`report_id`, `material_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工伤证明材料表';

-- ---------------------------------------------------------------------
-- 8. 工伤保险资料表（劳务公司补充，一单一档）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `injury_insurance`;
CREATE TABLE `injury_insurance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_id` bigint(20) NOT NULL COMMENT '工伤上报单ID',
  `insured_name` varchar(50) DEFAULT NULL COMMENT '参保人姓名',
  `insurance_company` varchar(100) DEFAULT NULL COMMENT '承保保险公司/社保经办机构',
  `policy_no` varchar(80) DEFAULT NULL COMMENT '保单号/社保电脑号',
  `coverage_start_date` date DEFAULT NULL COMMENT '保险起保日期',
  `coverage_end_date` date DEFAULT NULL COMMENT '保险终止日期',
  `claim_status` varchar(20) DEFAULT 'not_filed' COMMENT '理赔状态（not_filed-未报案，reported-已报案，claiming-理赔中，paid-已赔付，rejected-拒赔）',
  `claim_no` varchar(80) DEFAULT NULL COMMENT '报案号/理赔受理号',
  `claim_amount` decimal(12,2) DEFAULT NULL COMMENT '理赔金额（元）',
  `contact_name` varchar(50) DEFAULT NULL COMMENT '劳务经办人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '经办人电话',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除（0-否，1-是）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工伤保险资料表';

-- ---------------------------------------------------------------------
-- 9. 排班档案事件表（工伤上报、停工、复工等动作留痕，回到排班档案）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `schedule_event`;
CREATE TABLE `schedule_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `schedule_id` bigint(20) NOT NULL COMMENT '排班档案ID',
  `report_id` bigint(20) DEFAULT NULL COMMENT '关联工伤上报单ID',
  `event_type` varchar(30) NOT NULL COMMENT '事件类型（injury_report-工伤上报，injury_stop-停工结论，injury_resume-复工结论）',
  `event_content` varchar(1000) DEFAULT NULL COMMENT '事件内容',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `event_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除（0-否，1-是）',
  PRIMARY KEY (`id`),
  KEY `idx_evt_schedule` (`schedule_id`),
  KEY `idx_evt_report` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班档案事件表';

-- =====================================================================
-- 演示数据
-- =====================================================================

-- 项目
INSERT INTO `project` (`id`, `project_name`, `enterprise_name`, `labor_company`, `address`) VALUES
(1, '滨江会展中心二期', '华东建工集团有限公司', '安达劳务派遣有限公司', '上海市浦东新区滨江大道 1688 号'),
(2, '云栖智造产业园厂房', '云栖制造科技有限公司', '恒信人力资源服务有限公司', '杭州市余杭区云栖路 99 号'),
(3, '临港物流园仓储项目', '港联物流股份有限公司', '安达劳务派遣有限公司', '上海市浦东新区临港大道 600 号');

-- 工人
INSERT INTO `worker` (`id`, `worker_name`, `phone`, `id_card`, `labor_company`) VALUES
(1, '张铁柱', '13800000001', '340123199001011234', '安达劳务派遣有限公司'),
(2, '李志强', '13800000002', '340123199203052345', '安达劳务派遣有限公司'),
(3, '王建国', '13800000003', '410123198807123456', '恒信人力资源服务有限公司'),
(4, '赵满仓', '13800000004', '410123199511204567', '恒信人力资源服务有限公司'),
(5, '孙大伟', '13800000005', '320123199105065678', '安达劳务派遣有限公司'),
(6, '周明远', '13800000006', '320123199409186789', '安达劳务派遣有限公司');

-- 排班档案（id=1 已关联一起工伤并处于停工，id=2 正常，其余为可选项）
INSERT INTO `schedule` (`id`, `project_id`, `worker_id`, `post_name`, `shift`, `schedule_date`, `start_time`, `end_time`,
                        `schedule_status`, `injury_report_id`, `stop_start_date`, `resume_date`, `conclusion`, `remark`) VALUES
(1, 1, 1, '架子工', 'day', '2026-09-10', '2026-09-10 07:30:00', NULL, 'injury_stop', 1, '2026-09-10', NULL, 'stop', '搭设外架时踩空跌落，工伤停工'),
(2, 1, 2, '电焊工', 'day', '2026-09-11', '2026-09-11 07:30:00', NULL, 'normal', NULL, NULL, NULL, NULL, NULL),
(3, 1, 5, '普工', 'night', '2026-09-11', '2026-09-11 19:30:00', NULL, 'normal', NULL, NULL, NULL, NULL, NULL),
(4, 2, 3, '安装工', 'day', '2026-09-11', '2026-09-11 08:00:00', NULL, 'normal', NULL, NULL, NULL, NULL, NULL),
(5, 2, 4, '普工', 'middle', '2026-09-11', '2026-09-11 15:00:00', NULL, 'normal', NULL, NULL, NULL, NULL, NULL),
(6, 3, 6, '分拣员', 'night', '2026-09-11', '2026-09-11 20:00:00', NULL, 'normal', NULL, NULL, NULL, NULL, NULL);

-- 角色账号：主管 / 劳务 / 企业（admin 仍为 admin/123456）
UPDATE `user` SET `role` = 'admin' WHERE `username` = 'admin';
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `phone`, `project_id`, `company_name`)
SELECT 'supervisor1','123456','刘主管','supervisor','13900000001',1,NULL FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `user` u WHERE u.username='supervisor1');
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `phone`, `project_id`, `company_name`)
SELECT 'labor1','123456','安达劳务-陈经办','labor','13900000002',NULL,'安达劳务派遣有限公司' FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `user` u WHERE u.username='labor1');
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `phone`, `project_id`, `company_name`)
SELECT 'enterprise1','123456','华东建工-安全科','enterprise','13900000003',1,NULL FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `user` u WHERE u.username='enterprise1');

-- 工伤上报单：单1已上报但缺保险/合同等材料；单2为草稿演示缺件提示
INSERT INTO `injury_report`
(`id`, `report_no`, `project_id`, `schedule_id`, `worker_id`, `worker_name`, `post_name`, `shift_name`,
 `injury_time`, `injury_location`, `injury_desc`, `injury_type`, `hospital`, `hospital_time`,
 `reporter_id`, `reporter_name`, `report_time`, `report_status`, `material_progress`)
VALUES
(1, 'GS202609100001', 1, 1, 1, '张铁柱', '架子工', 'day',
 '2026-09-10 10:15:00', '滨江会展中心二期 3#楼东侧 6 层外架',
 '在 6 层搭设外脚手架时踩空滑倒，左手本能撑地，致左手腕肿痛畸形，疑似骨折；现场停工并由主管送医。',
 'hospitalized', '上海市第七人民医院 急诊骨科', '2026-09-10 10:55:00',
 (SELECT id FROM `user` WHERE username='supervisor1'), '刘主管', '2026-09-10 11:20:00', 'reported', 30),
(2, 'GS202609110002', 1, 3, 5, '孙大伟', '普工', 'night',
 '2026-09-11 02:40:00', '滨江会展中心二期 材料堆场',
 '夜间搬运钢筋捆时右脚被滑落钢筋砸伤，脚背淤青肿胀，暂能行走，待送医拍片。',
 'outpatient', '', NULL,
 (SELECT id FROM `user` WHERE username='supervisor1'), '刘主管', '2026-09-11 03:10:00', 'draft', 0);

-- 见证人
INSERT INTO `injury_witness` (`report_id`, `witness_name`, `witness_phone`, `witness_type`, `statement`) VALUES
(1, '李志强', '13800000002', 'coworker', '当时在同一作业面下方递料，亲眼看到张铁柱脚踩空、左手撑地，随后无法继续作业。'),
(1, '王安全', '13900001111', 'manager', '接到班组报告后约 3 分钟到达现场，组织拍照、停工，安排车辆送七院急诊。');

-- 已有材料：单1仅上传了现场照片、事故经过说明、急诊诊断（缺劳动合同、参保证明、病历、票据等）
INSERT INTO `injury_material`
(`report_id`, `material_type`, `material_name`, `file_name`, `original_name`, `file_path`, `file_url`, `file_size`, `file_ext`,
 `owner_role`, `upload_user_id`, `upload_user_name`, `remark`)
VALUES
(1, 'site_photo', '事故现场照片', 'demo/site_photo_1.jpg', '现场_东侧外架.jpg', 'demo/site_photo_1.jpg', '', 245123, '.jpg',
 'supervisor', (SELECT id FROM `user` WHERE username='supervisor1'), '刘主管', '6层外架踏空点与坠落位置'),
(1, 'accident_report', '事故经过书面报告', 'demo/accident_report_1.pdf', '9·10事故经过.pdf', 'demo/accident_report_1.pdf', '', 102400, '.pdf',
 'supervisor', (SELECT id FROM `user` WHERE username='supervisor1'), '刘主管', '项目部盖章扫描件'),
(1, 'medical_diagnosis', '医疗诊断证明', 'demo/diagnosis_1.jpg', '急诊诊断证明.jpg', 'demo/diagnosis_1.jpg', '', 312044, '.jpg',
 'common', 2, '刘主管', '七院急诊：左桡骨远端骨折');

-- 单1保险资料（劳务已填一半，未上传保单与劳动合同）
INSERT INTO `injury_insurance`
(`report_id`, `insured_name`, `insurance_company`, `policy_no`, `coverage_start_date`, `coverage_end_date`,
 `claim_status`, `contact_name`, `contact_phone`, `remark`)
VALUES
(1, '张铁柱', '中国人民财产保险股份有限公司上海分公司', 'PZBG2026SH038821', '2026-03-01', '2027-02-28',
 'reported', '陈经办', '13900000002', '项目团体意外险，9月10日已电话报案，受理短信待补传');

-- 排班档案事件（单1上报 + 停工已留痕）
INSERT INTO `schedule_event`
(`schedule_id`, `report_id`, `event_type`, `event_content`, `operator_id`, `operator_name`, `event_time`) VALUES
(1, 1, 'injury_report', '主管手机端上报工伤：6层外架踩空，左手腕骨折，送上海市第七人民医院。', (SELECT id FROM `user` WHERE username='supervisor1'), '刘主管', '2026-09-10 11:20:00'),
(1, 1, 'injury_stop', '医疗诊断左桡骨远端骨折，医嘱制动休息6周，自2026-09-10起停工，预计2026-10-22复诊评估复工。', (SELECT id FROM `user` WHERE username='supervisor1'), '刘主管', '2026-09-10 16:00:00');
