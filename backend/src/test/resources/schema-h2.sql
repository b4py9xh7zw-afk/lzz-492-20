-- H2 (MySQL MODE) 测试用表结构，字段与 database/*.sql 保持一致
CREATE TABLE IF NOT EXISTS file_info (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  file_name VARCHAR(255), original_name VARCHAR(255), file_path VARCHAR(500),
  file_size BIGINT DEFAULT 0, file_type VARCHAR(50), file_extension VARCHAR(20),
  upload_user_id BIGINT, upload_user_name VARCHAR(50),
  create_time DATETIME, update_time DATETIME, deleted TINYINT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS work (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  work_name VARCHAR(100), work_content CLOB, work_status VARCHAR(20),
  work_time DATETIME, start_time DATETIME, end_time DATETIME,
  priority VARCHAR(20), remark VARCHAR(500),
  create_time DATETIME, update_time DATETIME, deleted TINYINT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS "user" (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50), password VARCHAR(100), nickname VARCHAR(50),
  role VARCHAR(20) DEFAULT 'admin', phone VARCHAR(20), project_id BIGINT, company_name VARCHAR(100),
  create_time DATETIME, update_time DATETIME, deleted TINYINT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS project (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_name VARCHAR(100), enterprise_name VARCHAR(100), labor_company VARCHAR(100),
  address VARCHAR(255), status VARCHAR(20) DEFAULT 'active',
  create_time DATETIME, update_time DATETIME, deleted TINYINT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS worker (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  worker_name VARCHAR(50), phone VARCHAR(20), id_card VARCHAR(30),
  labor_company VARCHAR(100), status VARCHAR(20) DEFAULT 'active',
  create_time DATETIME, update_time DATETIME, deleted TINYINT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS schedule (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id BIGINT, worker_id BIGINT, post_name VARCHAR(50), shift VARCHAR(20) DEFAULT 'day',
  schedule_date DATE, start_time DATETIME, end_time DATETIME,
  schedule_status VARCHAR(20) DEFAULT 'normal', injury_report_id BIGINT,
  stop_start_date DATE, resume_date DATE, conclusion VARCHAR(30), remark VARCHAR(500),
  create_time DATETIME, update_time DATETIME, deleted TINYINT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS injury_report (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  report_no VARCHAR(40), project_id BIGINT, schedule_id BIGINT, worker_id BIGINT,
  worker_name VARCHAR(50), post_name VARCHAR(50), shift_name VARCHAR(20),
  injury_time DATETIME, injury_location VARCHAR(255), injury_desc CLOB, injury_type VARCHAR(20) DEFAULT 'outpatient',
  hospital VARCHAR(150), hospital_time DATETIME, hospital_diagnosis VARCHAR(500),
  reporter_id BIGINT, reporter_name VARCHAR(50), report_time DATETIME,
  report_status VARCHAR(20) DEFAULT 'draft', conclusion VARCHAR(20), conclusion_remark VARCHAR(500),
  stop_start_date DATE, expected_resume_date DATE, actual_resume_date DATE,
  concluded_by VARCHAR(50), concluded_time DATETIME, material_progress INT DEFAULT 0,
  create_time DATETIME, update_time DATETIME, deleted TINYINT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS injury_witness (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  report_id BIGINT, witness_name VARCHAR(50), witness_phone VARCHAR(20),
  witness_type VARCHAR(20) DEFAULT 'coworker', statement VARCHAR(1000),
  create_time DATETIME, update_time DATETIME, deleted TINYINT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS injury_material (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  report_id BIGINT, material_type VARCHAR(40), material_name VARCHAR(100),
  file_name VARCHAR(255), original_name VARCHAR(255), file_path VARCHAR(500), file_url VARCHAR(500),
  file_size BIGINT DEFAULT 0, file_ext VARCHAR(20), owner_role VARCHAR(20) DEFAULT 'supervisor',
  upload_user_id BIGINT, upload_user_name VARCHAR(50), remark VARCHAR(500),
  create_time DATETIME, update_time DATETIME, deleted TINYINT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS injury_insurance (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  report_id BIGINT, insured_name VARCHAR(50), insurance_company VARCHAR(100), policy_no VARCHAR(80),
  coverage_start_date DATE, coverage_end_date DATE, claim_status VARCHAR(20) DEFAULT 'not_filed',
  claim_no VARCHAR(80), claim_amount DECIMAL(12,2), contact_name VARCHAR(50), contact_phone VARCHAR(20),
  remark VARCHAR(500), create_time DATETIME, update_time DATETIME, deleted TINYINT DEFAULT 0
);
CREATE TABLE IF NOT EXISTS schedule_event (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  schedule_id BIGINT, report_id BIGINT, event_type VARCHAR(30), event_content VARCHAR(1000),
  operator_id BIGINT, operator_name VARCHAR(50), event_time DATETIME,
  create_time DATETIME, update_time DATETIME, deleted TINYINT DEFAULT 0
);
