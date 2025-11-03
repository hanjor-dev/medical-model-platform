-- ===================================================================
-- 医学影像模型管理平台 - 数据库初始化脚本
-- 版本: v2.1
-- 创建时间: 2025-01-14
-- 最后更新: 2025-01-15
-- 创建人: hanjor
-- 说明: 用户和权限模块数据库表结构初始化 + 动态积分系统 + 修复系统配置和字典系统
-- ===================================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ===================================================================
-- 1. 用户基础信息表
-- ===================================================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT '角色(USER:普通用户 ADMIN:管理员 SUPER_ADMIN:超级管理员)',
  `parent_user_id` bigint DEFAULT NULL COMMENT '上级用户ID(ADMIN管理的子用户)',
  `referral_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个人推荐码',
  `referrer_user_id` bigint DEFAULT NULL COMMENT '推荐人用户ID',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录IP',
  `login_fail_count` int NOT NULL DEFAULT '0' COMMENT '登录失败次数',
  `login_lock_time` datetime DEFAULT NULL COMMENT '登录锁定时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_username` (`username`),
  UNIQUE KEY `uk_users_email` (`email`),
  UNIQUE KEY `uk_users_referral_code` (`referral_code`),
  KEY `idx_users_parent_user_id` (`parent_user_id`),
  KEY `idx_users_referrer_user_id` (`referrer_user_id`),
  KEY `idx_users_role` (`role`),
  KEY `idx_users_status` (`status`),
  KEY `idx_users_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础信息表';

-- ===================================================================
-- 1.1 团队与成员表（新增）
-- 说明：引入团队域模型；用户在任一时刻仅允许加入一个未删除的团队
-- ===================================================================
DROP TABLE IF EXISTS `teams`;
CREATE TABLE `teams` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '团队ID',
  `team_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '团队名称',
  `team_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '团队码（用于加入）',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '团队描述',
  `owner_user_id` bigint NOT NULL COMMENT '团队拥有者用户ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_teams_team_name` (`team_name`),
  UNIQUE KEY `uk_teams_team_code` (`team_code`),
  KEY `idx_teams_owner_user_id` (`owner_user_id`),
  KEY `idx_teams_status` (`status`),
  KEY `idx_teams_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队表';

DROP TABLE IF EXISTS `team_members`;
CREATE TABLE `team_members` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `team_role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '团队内角色(OWNER/ADMIN/MEMBER)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `joined_at` datetime DEFAULT NULL COMMENT '加入时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  `team_id_active` bigint GENERATED ALWAYS AS (CASE WHEN (`is_deleted` = 0) THEN `team_id` ELSE NULL END) VIRTUAL,
  `user_id_active` bigint GENERATED ALWAYS AS (CASE WHEN (`is_deleted` = 0) THEN `user_id` ELSE NULL END) VIRTUAL,
  UNIQUE KEY `uk_team_member_active` (`team_id_active`,`user_id_active`),
  UNIQUE KEY `uk_user_team_active` (`user_id_active`),
  KEY `idx_team_members_team_id` (`team_id`),
  KEY `idx_team_members_user_id` (`user_id`),
  KEY `idx_team_members_role` (`team_role`),
  KEY `idx_team_members_status` (`status`),
  KEY `idx_team_members_team_status` (`team_id`, `status`, `is_deleted`),
  KEY `idx_team_members_user_status` (`user_id`, `status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队成员表';

-- 1.3 团队配置表（新增）
DROP TABLE IF EXISTS `team_configs`;
CREATE TABLE `team_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `config_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置值',
  `config_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'STRING' COMMENT '配置类型(STRING/NUMBER/BOOLEAN/JSON)',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置描述',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_configs_key` (`team_id`, `config_key`, `is_deleted`),
  KEY `idx_team_configs_team_id` (`team_id`),
  KEY `idx_team_configs_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队配置表';

-- 2.1 团队加入申请表（新增）
DROP TABLE IF EXISTS `team_join_requests`;
CREATE TABLE `team_join_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `user_id` bigint NOT NULL COMMENT '申请用户ID',
  `team_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '使用的团队码',
  `request_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '申请理由',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态(0:待审批 1:已通过 2:已拒绝)',
  `processed_by` bigint DEFAULT NULL COMMENT '处理人用户ID',
  `processed_at` datetime DEFAULT NULL COMMENT '处理时间',
  `process_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理理由',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_join_user_status_active` (`team_id`, `user_id`, `status`, `is_deleted`),
  KEY `idx_team_join_requests_team_id` (`team_id`),
  KEY `idx_team_join_requests_user_id` (`user_id`),
  KEY `idx_team_join_requests_status` (`status`),
  KEY `idx_team_join_requests_create_time` (`create_time`),
  KEY `idx_team_join_requests_team_status` (`team_id`, `status`, `is_deleted`),
  KEY `idx_team_join_requests_user_status` (`user_id`, `status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队加入申请表';

-- 2.2 团队邀请表（新增）
DROP TABLE IF EXISTS `team_invitations`;
CREATE TABLE `team_invitations` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '邀请ID',
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `invited_user_id` bigint DEFAULT NULL COMMENT '被邀请用户ID（可为空，通过邮箱邀请）',
  `invited_email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '被邀请邮箱',
  `invited_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '被邀请手机号',
  `inviter_user_id` bigint NOT NULL COMMENT '邀请人用户ID',
  `team_role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MEMBER' COMMENT '邀请的团队角色',
  `invitation_token` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邀请令牌',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态(0:待接受 1:已接受 2:已拒绝 3:已过期)',
  `accepted_at` datetime DEFAULT NULL COMMENT '接受时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_invitations_token` (`invitation_token`),
  KEY `idx_team_invitations_team_id` (`team_id`),
  KEY `idx_team_invitations_invited_user_id` (`invited_user_id`),
  KEY `idx_team_invitations_invited_email` (`invited_email`),
  KEY `idx_team_invitations_status` (`status`),
  KEY `idx_team_invitations_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队邀请表';

-- 4.1 团队积分账户表（新增）
-- 移除团队积分账户表（team_credits）
-- 已废弃：团队层积分能力下线

-- ===================================================================
-- 2. 积分类型配置表
-- ===================================================================
DROP TABLE IF EXISTS `credit_types`;
CREATE TABLE `credit_types` (
  `id` bigint NOT NULL COMMENT '积分类型ID',
  `type_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '积分类型编码(NORMAL/PREMIUM/READING等)',
  `type_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '积分类型名称',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci COMMENT '类型描述',
  `unit_name` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT '积分' COMMENT '积分单位名称',
  `icon_url` varchar(500) COLLATE utf8mb4_unicode_ci COMMENT '积分图标URL',
  `color_code` varchar(20) COLLATE utf8mb4_unicode_ci COMMENT '显示颜色',
  `decimal_places` int DEFAULT 0 COMMENT '小数位数(0表示整数)',
  `is_transferable` tinyint DEFAULT 1 COMMENT '是否支持转账(0:否 1:是)',
  `status` tinyint DEFAULT 1 COMMENT '状态(0:禁用 1:启用)',
  `sort_order` int DEFAULT 0 COMMENT '显示排序',
  `is_deleted` tinyint DEFAULT 0 COMMENT '删除标记',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `type_code_active` varchar(50) GENERATED ALWAYS AS (CASE WHEN (`is_deleted` = 0) THEN `type_code` ELSE NULL END) VIRTUAL,
  `type_name_active` varchar(100) GENERATED ALWAYS AS (CASE WHEN (`is_deleted` = 0) THEN `type_name` ELSE NULL END) VIRTUAL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_credit_types_type_code_active` (`type_code_active`),
  UNIQUE KEY `uk_credit_types_type_name_active` (`type_name_active`),
  KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分类型配置表';

-- ===================================================================
-- 3. 积分使用场景配置表
-- ===================================================================
DROP TABLE IF EXISTS `credit_usage_scenarios`;
CREATE TABLE `credit_usage_scenarios` (
  `id` bigint NOT NULL COMMENT '场景ID',
  `scenario_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场景编码(AI_COMPUTE/READING/UPLOAD等)',
  `scenario_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场景名称',
  `credit_type_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '使用的积分类型',
  `cost_per_use` decimal(10,2) NOT NULL COMMENT '每次使用消耗积分数',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci COMMENT '场景描述',
  `daily_limit` int COMMENT '每日使用次数限制',
  `user_roles` varchar(200) COLLATE utf8mb4_unicode_ci COMMENT '允许的用户角色(逗号分隔)',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `is_deleted` tinyint DEFAULT 0 COMMENT '删除标记',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `scenario_code_active` varchar(50) GENERATED ALWAYS AS (CASE WHEN (`is_deleted` = 0) THEN `scenario_code` ELSE NULL END) VIRTUAL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scenario_code_active` (`scenario_code_active`),
  KEY `idx_credit_type` (`credit_type_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分使用场景配置表';

-- ===================================================================
-- 4. 用户积分账户表(修改为支持动态积分类型)
-- ===================================================================
DROP TABLE IF EXISTS `user_credits`;
CREATE TABLE `user_credits` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `credit_type_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '积分类型编码',
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '积分余额',
  `total_earned` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '累计获得积分',
  `total_consumed` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '累计消费积分',
  `version` int NOT NULL DEFAULT '0' COMMENT '版本号(乐观锁)',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `user_id_active` bigint GENERATED ALWAYS AS (CASE WHEN (`is_deleted` = 0) THEN `user_id` ELSE NULL END) VIRTUAL,
  `credit_type_code_active` varchar(50) GENERATED ALWAYS AS (CASE WHEN (`is_deleted` = 0) THEN `credit_type_code` ELSE NULL END) VIRTUAL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_credits_active` (`user_id_active`, `credit_type_code_active`),
  KEY `idx_user_credits_user_id` (`user_id`),
  KEY `idx_user_credits_credit_type` (`credit_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户积分账户表';

-- ===================================================================
-- 5. 积分交易记录表(修改为支持动态积分类型)
-- ===================================================================
DROP TABLE IF EXISTS `credit_transactions`;
CREATE TABLE `credit_transactions` (
  `id` bigint NOT NULL COMMENT '交易ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `credit_type_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '积分类型编码',
  `transaction_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '交易类型(REGISTER:注册奖励 REFERRAL:推荐奖励 TRANSFER:转账 CONSUME:消费 REFUND:退款)',
  `amount` decimal(10,2) NOT NULL COMMENT '交易金额(正数为收入，负数为支出)',
  `balance_before` decimal(10,2) NOT NULL COMMENT '交易前余额',
  `balance_after` decimal(10,2) NOT NULL COMMENT '交易后余额',
  `related_user_id` bigint DEFAULT NULL COMMENT '关联用户ID(转账、推荐等)',
  `related_order_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联订单号',
  `related_transaction_id` bigint DEFAULT NULL COMMENT '关联交易ID(用于转账等双向交易)',
  `scenario_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '使用场景编码',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '交易描述',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `idempotency_key` varchar(255) GENERATED ALWAYS AS (CASE WHEN ((`related_order_id` IS NOT NULL) AND (`is_deleted` = 0)) THEN CONCAT(`user_id`, '#', `related_order_id`, '#', `transaction_type`) ELSE NULL END) VIRTUAL,
  PRIMARY KEY (`id`),
  KEY `idx_credit_transactions_user_id` (`user_id`),
  KEY `idx_credit_transactions_transaction_type` (`transaction_type`),
  KEY `idx_credit_transactions_create_time` (`create_time`),
  KEY `idx_credit_transactions_related_user_id` (`related_user_id`),
  KEY `idx_credit_transactions_related_order_id` (`related_order_id`),
  KEY `idx_credit_transactions_related_transaction_id` (`related_transaction_id`),
  KEY `idx_credit_transactions_scenario_code` (`scenario_code`),
  KEY `idx_credit_tx_user_scenario_type_time` (`user_id`, `scenario_code`, `transaction_type`, `create_time`),
  UNIQUE KEY `uk_credit_tx_idem` (`idempotency_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分交易记录表';


-- ===================================================================
-- 5.1 积分兑换码表（新增）
-- 说明：用于生成与管理积分兑换码，支持单次兑换
-- ===================================================================
DROP TABLE IF EXISTS `credit_redeem_codes`;
CREATE TABLE `credit_redeem_codes` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `code_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '兑换码显示KEY（加密/签名串）',
  `credit_type_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '积分类型编码',
  `amount` decimal(10,2) NOT NULL COMMENT '兑换积分数量（正数）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态(0:待使用 1:已兑换 2:已失效 3:已作废)',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间（到期后不可使用）',
  `remark` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注说明',
  `created_by` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `created_by_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人名称',
  `redeemed_by` bigint DEFAULT NULL COMMENT '兑换人用户ID',
  `redeemed_by_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '兑换人名称',
  `redeemed_time` datetime DEFAULT NULL COMMENT '兑换时间',
  `version` int NOT NULL DEFAULT '0' COMMENT '版本号(乐观锁)',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_credit_redeem_codes_code_key` (`code_key`),
  KEY `idx_credit_redeem_codes_status` (`status`),
  KEY `idx_credit_redeem_codes_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分兑换码表';

-- ===================================================================
-- 6. 权限配置表
-- ===================================================================
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
  `id` bigint NOT NULL COMMENT '权限ID',
  `permission_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `permission_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `permission_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限类型(MENU:菜单 BUTTON:按钮 API:接口)',
  `parent_id` bigint DEFAULT NULL COMMENT '父权限ID',
  `path` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜单路径',
  `component` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(10000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标(SVG代码)',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permissions_permission_code` (`permission_code`),
  KEY `idx_permissions_parent_id` (`parent_id`),
  KEY `idx_permissions_permission_type` (`permission_type`),
  KEY `idx_permissions_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限配置表';

-- ===================================================================
-- 7. 角色权限关联表
-- ===================================================================
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permissions_role_permission` (`role`, `permission_id`, `is_deleted`),
  KEY `idx_role_permissions_role` (`role`),
  KEY `idx_role_permissions_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ===================================================================
-- 7.1 用户权限覆盖表（新增）
-- ===================================================================
DROP TABLE IF EXISTS `user_permissions`;
CREATE TABLE `user_permissions` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `grant_type` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '授权类型(ALLOW/DENY)',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_permissions_user_perm` (`user_id`, `permission_id`, `is_deleted`),
  KEY `idx_user_permissions_user_id` (`user_id`),
  KEY `idx_user_permissions_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户权限覆盖表';

-- ===================================================================
-- 7.2 用户权限贡献表（新增）
-- 说明：物化"角色等来源"对用户权限的贡献；与用户手工覆盖(user_permissions)分离
-- ===================================================================
DROP TABLE IF EXISTS `user_permission_contrib`;
CREATE TABLE `user_permission_contrib` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `source_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '贡献来源类型(如：ROLE)',
  `source_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '贡献来源标识(如：角色名)',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_perm_contrib` (`user_id`, `permission_id`, `source_type`, `source_id`, `is_deleted`),
  KEY `idx_upc_user_id` (`user_id`),
  KEY `idx_upc_permission_id` (`permission_id`),
  KEY `idx_upc_source` (`source_type`, `source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户权限贡献表';

-- ===================================================================
-- 8. 系统配置表(修复版)
-- ===================================================================
DROP TABLE IF EXISTS `system_configs`;
CREATE TABLE `system_configs` (
  `id` bigint NOT NULL COMMENT '配置ID',
  `config_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置值',
  `config_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置类型',
   `config_type_code` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置类型编码',
  `config_category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置分类',
  `config_category_code` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置分类编码',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `sort_order` int DEFAULT 0 COMMENT '排序，数值越小排序越靠前',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_system_configs_config_key` (`config_key`),
  KEY `idx_config_category` (`config_category`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ===================================================================
-- 9. 通知与公告模块 - 用户通知偏好（重构为两表）
-- ===================================================================

-- 9.0 用户通知渠道偏好表（user_notify_channel_prefs）
DROP TABLE IF EXISTS `user_notify_channel_prefs`;
CREATE TABLE `user_notify_channel_prefs` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `channel_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道编码(inbox/email/sms)',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否开启该渠道(0:否 1:是)',
  `do_not_disturb` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '免打扰时段(HH:mm-HH:mm，仅对部分渠道生效，如email)',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_channel_pref_active` (`user_id`, `channel_code`, `is_deleted`),
  KEY `idx_user_channel_pref_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知渠道偏好表';

-- 9.0.1 用户通知类型偏好表（user_notify_type_prefs）
DROP TABLE IF EXISTS `user_notify_type_prefs`;
CREATE TABLE `user_notify_type_prefs` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息类型编码(system/task/credit/marketing等)',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否开启该类型(0:否 1:是)',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_type_pref_active` (`user_id`, `type_code`, `is_deleted`),
  KEY `idx_user_type_pref_user` (`user_id`),
  KEY `idx_user_type_pref_type` (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知类型偏好表';

-- ===================================================================
-- 9.1 公告表（announcements）
-- ===================================================================
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements` (
  `id` bigint NOT NULL COMMENT '公告ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` longtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '富文本内容',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级（越大越靠前）',
  `force_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否强制阅读(0:否 1:是)',
  `active_from` datetime DEFAULT NULL COMMENT '生效时间（可空）',
  `active_to` datetime DEFAULT NULL COMMENT '失效时间（可空）',
  `schedule_time` datetime DEFAULT NULL COMMENT '定时发布时间（可空）',
  `notified` tinyint NOT NULL DEFAULT '0' COMMENT '是否已进行到点首次推送(0:否 1:是)',
  `first_push_time` datetime DEFAULT NULL COMMENT '首次到点推送时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态(0:草稿 1:已发布 2:已下线)',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_announcements_status_time` (`status`, `active_from`, `active_to`),
  KEY `idx_announcements_notified` (`notified`),
  KEY `idx_announcements_priority` (`priority`),
  KEY `idx_announcements_schedule_time` (`schedule_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- -------------------------------------------------------------------
-- 9.1.1 公告阅读明细表（announcement_reads）
-- -------------------------------------------------------------------
DROP TABLE IF EXISTS `announcement_reads`;
CREATE TABLE `announcement_reads` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `announcement_id` bigint NOT NULL COMMENT '公告ID',
  `user_id` bigint NOT NULL COMMENT '接收人用户ID',
  `read_at` datetime DEFAULT NULL COMMENT '阅读时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_announcement_read_user` (`announcement_id`, `user_id`),
  KEY `idx_announcement_read_user_time` (`user_id`, `read_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告阅读明细表';

-- （移除）旧版 messages 表已废弃，使用 notify_messages 取代

-- （移除）旧版 message_recipients 表已废弃，由更细粒度的偏好与推送表替代

-- （删除）9.4 模板表 notify_templates 已取消，改为消息直接写入内容

-- 9.5 推送任务表（notify_push_tasks）
DROP TABLE IF EXISTS `notify_push_tasks`;
CREATE TABLE `notify_push_tasks` (
  `id` bigint NOT NULL COMMENT '任务ID',
  `message_id` bigint NOT NULL COMMENT '关联消息ID',
  `scheduled_time` datetime DEFAULT NULL COMMENT '计划执行时间',
  `next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
  `attempt_count` int NOT NULL DEFAULT '0' COMMENT '尝试次数',
  `max_attempts` int NOT NULL DEFAULT '3' COMMENT '最大尝试次数',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态(0:待执行 1:执行中 2:成功 3:失败 4:已取消)',
  `last_error` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一次错误信息',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_notify_push_tasks_status_time` (`status`, `scheduled_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推送任务表';

-- （移除）旧版 push_logs 表已废弃

-- -------------------------------------------------------------------
-- 9.6 用户消息阅读明细表（notify_message_reads）
-- -------------------------------------------------------------------
DROP TABLE IF EXISTS `notify_message_reads`;
CREATE TABLE `notify_message_reads` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `message_id` bigint NOT NULL COMMENT '消息ID',
  `user_id` bigint NOT NULL COMMENT '接收人用户ID',
  `read_at` datetime NOT NULL COMMENT '阅读时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notify_message_read_user` (`message_id`, `user_id`),
  KEY `idx_notify_message_read_user_time` (`user_id`, `read_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户消息阅读明细表';

-- ===================================================================
-- 9.7 用户定向通知消息表（notify_messages）
-- 与实体 com.okbug.platform.entity.system.message.Message 一致
-- ===================================================================
DROP TABLE IF EXISTS `notify_messages`;
CREATE TABLE `notify_messages` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `message_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息类型编码(system/task/credit/marketing)',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息标题（可空）',
  `content` longtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息正文内容（直接存储）',
  `schedule_time` datetime DEFAULT NULL COMMENT '计划推送时间（可空，null表示立即）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0待调度 1已入队 2已完成 3已取消 4失败',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除(0:正常 1:删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_notify_messages_user` (`user_id`),
  KEY `idx_notify_messages_type` (`message_type`),
  KEY `idx_notify_messages_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户定向通知消息（直接内容）';

-- ===================================================================
-- 10. 操作日志表
-- ===================================================================
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs` (
  `id` bigint NOT NULL COMMENT '日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作用户名',
  `operation_module` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作模块',
  `operation_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `operation_desc` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作描述',
  `request_method` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求URL',
  `request_params` text COLLATE utf8mb4_unicode_ci COMMENT '请求参数',
  `response_result` text COLLATE utf8mb4_unicode_ci COMMENT '响应结果',
  `operation_ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作IP',
  `operation_location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作地点',
  `operation_status` tinyint NOT NULL DEFAULT '1' COMMENT '操作状态(0:失败 1:成功)',
  `error_message` text COLLATE utf8mb4_unicode_ci COMMENT '错误消息',
  `operation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `cost_time` bigint DEFAULT NULL COMMENT '耗时(毫秒)',
  PRIMARY KEY (`id`),
  KEY `idx_operation_logs_user_id` (`user_id`),
  KEY `idx_operation_logs_operation_time` (`operation_time`),
  KEY `idx_operation_logs_operation_module` (`operation_module`),
  KEY `idx_operation_logs_operation_type` (`operation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ===================================================================
-- 11. 系统字典表(修复版)
-- ===================================================================
DROP TABLE IF EXISTS `system_dict`;
CREATE TABLE `system_dict` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_code` varchar(200) NOT NULL COMMENT '字典编码(使用path值，如：DICT_1.2)',
  `dict_name` varchar(200) NOT NULL COMMENT '字典名称，如：字符串类型',
  `dict_label` varchar(200) DEFAULT NULL COMMENT '字典标签，如：字符串',
  `description` varchar(500) DEFAULT NULL COMMENT '字典描述',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父级字典ID，0表示顶级字典',
  `level` int(11) DEFAULT 1 COMMENT '字典层级，1表示顶级字典',
  `path` varchar(500) DEFAULT NULL COMMENT '字典路径，如：1.2.3',
  `module` varchar(50) DEFAULT 'SYSTEM' COMMENT '所属模块，如：SYSTEM、USER、FILE',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '字典状态，0：禁用，1：启用',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序，数值越小排序越靠前',
  `is_deleted` int(11) DEFAULT 0 COMMENT '是否删除，0：未删除，1：已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_module` (`module`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典表';

-- ===================================================================
-- 12. 配置变更日志表(新增)
-- ===================================================================
DROP TABLE IF EXISTS `config_change_logs`;
CREATE TABLE `config_change_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_id` bigint NOT NULL COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `old_value` text COMMENT '修改前值',
  `new_value` text NOT NULL COMMENT '修改后值',
  `change_type` varchar(20) NOT NULL COMMENT '变更类型(CREATE:创建 UPDATE:修改 DELETE:删除)',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) NOT NULL COMMENT '操作人姓名',
  `operation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `ip_address` varchar(50) DEFAULT NULL COMMENT '操作IP',
  PRIMARY KEY (`id`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_config_key` (`config_key`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置变更日志表';

-- ===================================================================
-- 初始化基础数据
-- ===================================================================

-- 初始化积分类型数据
INSERT INTO `credit_types` (`id`, `type_code`, `type_name`, `description`, `unit_name`, `icon_url`, `color_code`, `decimal_places`, `is_transferable`, `status`, `sort_order`) VALUES
(1, 'NORMAL', '普通积分', '用户阅片等基础功能使用', '积分', '/icons/normal-credit.png', '#333333', 0, 1, 1, 1),
(2, 'PREMIUM', '高级积分', 'AI计算等高级功能使用', '高级积分', '/icons/premium-credit.png', '#B8860B', 0, 1, 1, 2);

-- 初始化积分使用场景数据
INSERT INTO `credit_usage_scenarios` (`id`, `scenario_code`, `scenario_name`, `credit_type_code`, `cost_per_use`, `description`, `daily_limit`, `user_roles`, `status`) VALUES
-- 消费场景
(1, 'FILM', '用户阅片', 'NORMAL', 1.00, '用户查看和分析医学影像', NULL, 'USER,ADMIN,SUPER_ADMIN', 1),
(2, 'AI_COMPUTE', 'AI计算', 'PREMIUM', 1.00, 'AI模型计算和处理', NULL, 'USER,ADMIN,SUPER_ADMIN', 1),

-- 奖励场景
(3, 'USER_REGISTER', '用户注册', 'NORMAL', -10.00, '新用户注册奖励', NULL, 'USER,ADMIN,SUPER_ADMIN', 1),
(4, 'USER_REFERRAL', '用户推荐', 'NORMAL', -10.00, '推荐新用户奖励', NULL, 'USER,ADMIN,SUPER_ADMIN', 1),

-- 特殊场景
(5, 'ADMIN_GRANT', '管理员发放', 'NORMAL', 0.00, '管理员手动发放的积分（面额由管理员决定，积分类型不限）', NULL, 'SUPER_ADMIN', 1),

-- 兑换码场景（奖励型，额度与类型由兑换码决定，场景本身不设定固定额度）
(6, 'REDEEM_CODE', '兑换码', 'NORMAL', 0.00, '兑换码兑换获得积分（面额由兑换码决定，积分类型不限）', NULL, 'USER,ADMIN,SUPER_ADMIN', 1);

-- 初始化权限数据
INSERT INTO `permissions` (`id`, `permission_code`, `permission_name`, `permission_type`, `parent_id`, `path`, `component`, `icon`, `sort`, `status`) VALUES
-- 一级菜单权限（顶级菜单）
(1, 'dashboard', '控制台', 'MENU', NULL, '/dashboard', 'dashboard/index', '<svg t="1756387085906" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="46943" width="20" height="20"><path d="M810.666667 213.333333l-85.333334-54.186666V42.666667h85.333334zM810.666667 512h85.333333v-53.717333l-384-268.8-384 268.8V512h85.333333v384h128v-61.056a170.666667 170.666667 0 1 1 341.333334 0V896h128v-384z m85.333333 469.333333h-298.666667v-146.389333a85.333333 85.333333 0 1 0-170.666666 0V981.333333H128v-384H42.666667V413.866667L512 85.333333l469.333333 328.533334V597.333333h-85.333333v384z" fill="currentColor" p-id="46944"></path></svg>', 1, 1),
(2, 'model-management', '模型管理', 'MENU', NULL, '/model-management', 'Layout', '<svg t="1756477384338" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="7710" width="20" height="20"><path d="M574.016 561.984l299.968-174.016V544h60.032v-192c0-24-12.032-41.984-30.016-54.016L574.016 112c-17.984-12.032-36.032-12.032-54.016 0l-336 192c-17.984 6.016-30.016 30.016-30.016 48v384c0 17.984 12.032 36.032 30.016 48l329.984 192a54.528 54.528 0 0 0 60.032 0l30.016-17.984v-72l-30.08 17.984V561.984zM513.92 910.08L214.016 736V387.968l299.968 174.08v347.968zM544 508.032l-300.032-174.08L544 160l300.032 174.016L544 507.968z" fill="currentColor" p-id="7711"></path><path d="M964.032 604.032h-240c-36.032 0-60.032 23.936-60.032 59.968v300.032c0 35.968 24 59.968 60.032 59.968h240c35.968 0 59.968-24 59.968-60.032v-299.968c0-36.032-24-60.032-60.032-60.032z m-210.048 299.968c-17.984 0-30.016-12.032-30.016-30.016 0-17.984 12.032-30.016 30.08-30.016 17.92 0 29.952 12.032 29.952 30.08 0 17.92-12.032 29.952-30.016 29.952z m0-120c-17.984 0-30.016-12.032-30.016-30.016 0-17.984 12.032-30.016 30.08-30.016 17.92 0 29.952 12.032 29.952 30.08 0 17.92-12.032 29.952-30.016 29.952z m209.984 120h-119.936v-60.032h119.936v60.032z m0-120h-119.936v-60.032h119.936v60.032z" fill="currentColor" p-id="7712"></path></svg>', 2, 1),
(3, 'credit-system', '积分系统', 'MENU', NULL, '/credit-system', 'Layout', '<svg t="1756477882867" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="19267" width="20" height="20"><path d="M162.005333 580.992L192.042667 640c-20.693333 20.010667-31.36 41.344-32 64 2.645333 42.666667 36.821333 79.658667 102.485333 111.018667 65.664 31.317333 148.821333 47.658667 249.514667 48.981333 100.693333-1.365333 183.850667-17.664 249.514666-48.981333 65.621333-31.317333 99.84-68.352 102.485334-111.018667-0.682667-22.656-11.349333-44.032-32-64l30.976-59.008c20.693333 17.322667 36.693333 36.352 48 57.002667 11.349333 20.650667 17.024 42.666667 17.024 66.005333-3.328 66.005333-44.373333 119.509333-123.008 160.512-78.677333 41.002667-176.341333 62.165333-292.992 63.488-116.693333-1.365333-214.357333-22.528-292.992-63.488-78.677333-40.96-119.68-94.464-123.008-160.512 0-23.338667 5.845333-45.354667 17.493333-66.005333 11.648-20.693333 27.818667-39.68 48.512-57.002667z m0-192l30.037334 59.008c-20.693333 20.010667-31.36 41.344-32 64 2.645333 42.666667 36.821333 79.658667 102.485333 111.018667 65.664 31.317333 148.821333 47.658667 249.514667 48.981333 100.693333-1.322667 183.850667-17.664 249.514666-48.981333C827.178667 591.701333 861.397333 554.666667 864 512c-0.682667-22.656-11.349333-44.032-32-64l30.976-59.008c20.693333 17.322667 36.693333 36.352 48 57.002667 11.349333 20.650667 17.024 42.666667 17.024 66.005333-3.328 66.005333-44.373333 119.509333-123.008 160.512-78.677333 41.002667-176.341333 62.165333-292.992 63.488-116.693333-1.365333-214.357333-22.528-292.992-63.488C140.373333 631.552 99.370667 578.048 96 512c0-23.338667 5.845333-45.354667 17.493333-66.005333 11.648-20.693333 27.818667-39.68 48.512-57.002667zM512 544c-116.693333-1.322667-214.357333-22.528-292.992-63.488-78.677333-40.96-119.68-94.464-123.008-160.512 3.328-66.005333 44.330667-119.509333 123.008-160.512C297.685333 118.485333 395.349333 97.322667 512 96c116.693333 1.322667 214.314667 22.528 292.992 63.488 78.634667 40.96 119.68 94.464 123.008 160.512-3.328 66.005333-44.330667 119.509333-123.008 160.512-78.677333 41.002667-176.341333 62.165333-292.992 63.488z m0-64c100.693333-1.322667 183.850667-17.664 249.514667-48.981333 65.664-31.317333 99.84-68.309333 102.485333-111.018667-2.688-42.666667-36.821333-79.658667-102.485333-111.018667C695.808 177.664 612.693333 161.322667 512 160c-100.693333 1.322667-183.850667 17.664-249.514667 48.981333C196.821333 240.298667 162.645333 277.333333 160 320c2.688 42.666667 36.821333 79.658667 102.485333 111.018667 65.706667 31.317333 148.821333 47.658667 249.514667 48.981333z" fill="currentColor" fill-opacity=".65" p-id="19268"></path></svg>', 3, 1),
(4, 'user-permission', '用户权限', 'MENU', NULL, '/user-permission', 'Layout', '<svg t="1756386116631" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="35353" width="20" height="20"><path d="M706.816 629.12a383.872 383.872 0 0 0-123.584-75.968 255.68 255.68 0 0 0 81.216-352.832 255.68 255.68 0 0 0-352.832-81.152A255.68 255.68 0 0 0 230.4 471.936c20.416 32.832 48.448 60.8 81.216 81.216A384.32 384.32 0 0 0 64 895.552c-0.384 8.768 6.4 16.384 15.232 16.768h32.768a16.192 16.192 0 0 0 16-15.168 320.128 320.128 0 0 1 534.848-221.632c6.4 5.632 16 5.632 21.952-0.384l22.848-23.168a16 16 0 0 0-0.832-22.848z m-259.2-101.12a192 192 0 1 1 192-192 192.576 192.576 0 0 1-192 192z m496 224h-24v-34.048c0-47.616-38.4-86.016-86.016-86.016h-3.968c-47.616 0-86.016 38.4-86.016 86.016v33.984h-24a16 16 0 0 0-16 16v160c0 8.832 7.232 16 16 16h224a16 16 0 0 0 16-16V768a16 16 0 0 0-16-16z m-152-34.048c0-20.8 17.216-38.016 38.016-38.016h3.968c20.8 0 38.016 17.216 38.016 38.016v33.984h-80v-33.984z m120 177.984h-160v-96h160V896z" fill="currentColor" p-id="35354"></path></svg>', 4, 1),
(25, 'team', '团队管理', 'MENU', NULL, '/team', 'Layout', '<svg t="1760410864831" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4635" width="20" height="20"><path d="M845.4 119.1H179.6L64.5 449l448 454.4 448-454.4-115.1-329.9zM559.1 657.8l-39.6 39.6-277.3-277.3 39.6-39.6 238.7 238.7 239.8-239.8 38.5 38.5-239.7 239.9z" p-id="4636"></path></svg>', 5, 1),
(5, 'ai-engine', 'AI引擎', 'MENU', NULL, '/ai-engine', 'Layout', '<svg t="1756477531011" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="14850" width="20" height="20"><path d="M883.2 346.88V292.266667L512 72.106667 140.8 292.266667V426.666667h59.733333V326.4L512 141.226667l311.466667 185.173333v20.48a85.333333 85.333333 0 1 0 59.733333 0zM823.466667 697.6L512 882.773333l-311.466667-185.173333v-20.48a85.333333 85.333333 0 1 0-59.733333 0v54.613333l371.2 220.16 371.2-220.16V597.333333h-59.733333z" fill="currentColor" p-id="14851"></path><path d="M450.133333 548.693333h57.6l-28.16-84.053333-29.44 84.053333z" fill="currentColor" p-id="14852"></path><path d="M725.333333 640V384l-213.333333-128-213.333333 128v256l213.333333 128z m-123.306666-243.2h45.226666v230.4h-45.226666z m-178.346667 230.4h-46.933333l85.333333-230.4h34.986667l85.333333 230.4H533.333333l-13.653333-42.666667h-81.92z" fill="currentColor" p-id="14853"></path></svg>', 6, 1),
(6, 'system', '系统管理', 'MENU', NULL, '/system', 'Layout', '<svg t="1756385868560" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="25704" width="20" height="20"><path d="M796.601376 533.883517c29.17676 9.213714 56.306028 25.081776 79.340312 45.556695-16.891808 45.044822 5.630603 95.208374 50.675425 112.612055 6.142476 2.047492 12.284951 4.094984 18.427427 4.606857 6.142476 29.688633 6.142476 60.912884 0 90.601517-47.604187 6.654349-80.875931 51.187298-73.709709 98.791485 1.023746 6.654349 2.559365 13.308697 5.11873 19.451173-23.034284 20.474919-49.651679 35.831109-79.340312 45.556695-30.200506-37.366727-85.482787-42.99733-122.849515-12.284951-5.11873 4.094984-9.725587 8.701841-13.82057 13.82057-29.17676-9.213714-56.306028-25.081776-79.340312-45.556695 18.427427-44.532949-3.071238-95.208374-47.092314-113.635801-6.654349-2.559365-13.308697-4.606857-19.963046-5.630603-6.142476-29.688633-6.142476-60.912884 0-90.601517 47.604187-7.166222 80.364058-51.699171 72.685963-99.303358-1.023746-6.142476-2.559365-11.773079-4.606857-17.915554 22.522411-20.474919 49.139806-35.831109 78.316566-45.556695 30.200506 37.366727 84.970914 42.99733 122.337642 12.796824 4.606857-4.094984 9.213714-8.189968 12.796824-12.796824M660.443164 460.685681c-7.678095-1.023746-15.356189-1.023746-22.522411 0-38.902346 12.796824-74.733455 33.271744-105.445834 60.912884-23.546157 20.986792-30.712379 54.770409-18.427427 83.435296 3.071238 7.166222-0.511873 15.356189-7.678095 17.915554-1.023746 0.511873-2.047492 0.511873-3.583111 1.023746-30.712379 4.094984-55.794155 27.129268-62.448503 57.841647-8.701841 40.949838-9.213714 82.923423 0 123.873261 6.142476 29.688633 30.200506 52.211044 59.889138 57.329773 7.678095 0.511873 13.82057 6.654349 13.308698 14.332444v1.535618c-10.749333 28.153014-3.071238 60.401011 19.963046 80.364058 30.712379 27.641141 66.543487 48.11606 105.445834 60.912885 28.664887 9.213714 59.889139 0.511873 79.340311-22.522411 4.606857-6.142476 13.308697-7.166222 19.451174-2.559365 1.023746 0.511873 1.535619 1.535619 2.559365 2.559365 19.451173 23.034284 50.675425 32.247998 78.828438 22.522411 38.902346-12.796824 74.733455-33.271744 105.445834-60.912885 22.522411-19.963046 30.712379-52.211044 19.963046-80.364058-3.071238-7.166222 0.511873-15.356189 7.678095-17.915554 1.023746-0.511873 2.047492-0.511873 3.583111-1.023746 30.200506-4.606857 54.258536-27.129268 60.912884-57.329773 8.189968-39.926092 8.189968-80.875931 0-120.802023-6.142476-30.712379-31.736125-54.258536-62.960376-57.841647-7.678095-1.023746-12.796824-8.189968-11.773079-15.868062 0-1.023746 0.511873-2.047492 1.023746-3.583111 10.23746-28.153014 2.559365-59.889139-19.963046-79.852185-30.712379-27.641141-66.543487-48.11606-105.445834-60.912884-28.664887-9.213714-59.889139-0.511873-79.340311 22.522411-4.606857 6.142476-13.308697 7.166222-19.451174 2.559365-1.023746-0.511873-1.535619-1.535619-2.559364-2.559365-13.82057-15.868062-34.29549-25.593649-55.794155-25.593649" p-id="25705"></path><path d="M728.52227 705.360965c19.963046 0 36.854854 16.379935 36.854854 36.854854s-16.379935 36.854854-36.854854 36.854855-36.854854-16.379935-36.854854-36.854855c0.511873-20.474919 16.891808-36.854854 36.854854-36.854854m0-73.197836c-60.912884 0-110.05269 49.139806-110.05269 110.05269s49.139806 110.05269 110.05269 110.052691 110.05269-49.139806 110.05269-110.052691-49.139806-110.05269-110.05269-110.05269M660.443164 183.250526c0 20.474919 16.379935 36.854854 36.854854 36.854855s36.854854-16.379935 36.854855-36.854855c0-19.963046-16.379935-36.854854-36.854855-36.854854s-36.854854 16.379935-36.854854 36.854854" p-id="25706"></path><path d="M400.411691 659.292397c20.474919 0 36.854854-16.379935 36.854854-36.854855s-16.379935-36.854854-36.854854-36.854854H74.348603V365.98918h732.490233v36.854854c0 20.474919 16.379935 36.854854 36.854854 36.854855 19.963046 0 36.854854-16.379935 36.854855-36.854855V73.197836C880.548545 36.342981 852.907404 4.606857 816.564422 0H65.646763C28.791908 4.606857 1.150767 35.831109 1.150767 73.197836v805.688068C0.638894 916.252632 28.791908 947.476884 65.646763 952.08374h338.348038c20.474919 0 36.854854-16.379935 36.854855-36.854854s-16.379935-36.854854-36.854855-36.854855H106.084728c-18.9393-1.535619-32.759871-17.915554-31.736125-36.854854v-183.250526h326.063088zM74.348603 110.05269c-1.023746-18.9393 12.796824-34.807363 31.736125-36.854854h669.529856c18.9393 1.535619 32.759871 17.915554 31.736125 36.854854v183.250527H74.348603V110.05269z" p-id="25707"></path></svg>', 7, 1),
(7, 'statistics', '数据统计', 'MENU', NULL, '/statistics', 'statistics/index', '<svg t="1756386166848" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="37261" width="20" height="20"><path d="M175.104 518.656h120.32v134.656H175.104zM729.088 303.104h120.32v350.72h-120.32z" fill="currentColor" p-id="37262"></path><path d="M957.44 28.672H66.56c-36.864 0-66.56 29.696-66.56 66.56v623.104c0 36.864 29.696 66.56 66.56 66.56h890.88c36.864 0 66.56-29.696 66.56-66.56V95.232c0-36.864-29.696-66.56-66.56-66.56z m0 667.648c0 12.288-9.728 22.016-22.016 22.016H89.088c-12.288 0-22.016-9.728-22.016-22.016V117.76c0-12.288 9.728-22.016 22.016-22.016h845.824c12.288 0 22.016 9.728 22.016 22.016v578.56z" fill="currentColor" p-id="37263"></path><path d="M344.064 410.624h120.32v242.688H344.064zM536.576 195.072h120.32v458.752h-120.32z" fill="currentColor" p-id="37264"></path></svg>', 8, 1),

-- 二级菜单权限（子菜单）
-- 模型管理子菜单
(8, 'model-management:task', '计算任务', 'MENU', 2, '/model-management/task', 'model-management/task/index', '', 1, 1),
(9, 'model-management:list', '模型列表', 'MENU', 2, '/model-management/list', 'model-management/list/index', '', 2, 1),
(10, 'model-management:category', '模型分类', 'MENU', 2, '/model-management/category', 'model-management/category/index', '', 3, 1),
(11, 'model-management:group', '模型分组', 'MENU', 2, '/model-management/group', 'model-management/group/index', '', 4, 1),

-- 积分系统子菜单
(12, 'credit-system:my-credits', '我的积分', 'MENU', 3, '/credit-system/my-credits', 'credit-system/my-credits/index', '', 1, 1),
(13, 'credit-system:user-credits', '用户积分', 'MENU', 3, '/credit-system/user-credits', 'credit-system/user-credits/index', '', 2, 1),
(14, 'credit-system:type-management', '类型管理', 'MENU', 3, '/credit-system/type-management', 'credit-system/type-management/index', '', 3, 1),
(15, 'credit-system:scenario-management', '场景管理', 'MENU', 3, '/credit-system/scenario-management', 'credit-system/scenario-management/index', '', 4, 1),

-- 用户权限子菜单
(16, 'user-permission:user', '用户列表', 'MENU', 4, '/user-permission/user', 'user-permission/user/index', '', 1, 1),
(18, 'user-permission:permission', '权限管理', 'MENU', 4, '/user-permission/permission', 'user-permission/permission/index', '', 3, 1),
(19, 'user-permission:log', '用户日志', 'MENU', 4, '/user-permission/log', 'user-permission/log/index', '', 4, 1),

-- 团队管理菜单与子菜单（移动至“用户权限”下方）

(26, 'team:members', '成员管理', 'MENU', 25, '/team/members', 'team/members/TeamMembers', '', 2, 1),
-- (27, 'team:invitations', '邀请管理', 'MENU', 25, '/team/invitations', 'team/invitations/TeamInvitations', '', 3, 1), -- 已下线，移除初始化
(28, 'team:join-requests', '加入申请', 'MENU', 25, '/team/join-requests', 'team/join-requests/TeamJoinRequests', '', 4, 1),
(29, 'team:my', '我的团队', 'MENU', 25, '/team/my', 'team/my/MyTeam', '', 1, 1),

-- AI引擎子菜单
(20, 'ai-engine:algorithm', '算法管理', 'MENU', 5, '/ai-engine/algorithm', 'ai-engine/algorithm/index', '', 1, 1),
(21, 'ai-engine:multi-config', '多端配置', 'MENU', 5, '/ai-engine/multi-config', 'ai-engine/multi-config/index', '', 2, 1),

-- 系统管理子菜单
(22, 'system:config', '系统配置', 'MENU', 6, '/system/config', 'system/config/index', '', 1, 1),
(23, 'system:dict', '字典数据', 'MENU', 6, '/system/dict', 'system/dict/index', '', 2, 1),
(24, 'system:message', '消息与公告', 'MENU', 6, '/system/message', 'system/message/index', '', 3, 1);

-- 团队管理菜单与子菜单（已上移至“用户权限”下方）

-- 初始化角色权限关联
-- USER角色权限（基础权限）
INSERT INTO `role_permissions` (`id`, `role`, `permission_id`) VALUES
(1, 'USER', 1),   -- 控制台
(2, 'USER', 2),   -- 模型管理
(3, 'USER', 8),   -- 计算任务
(4, 'USER', 9),   -- 模型列表
(5, 'USER', 10),  -- 模型分类
(6, 'USER', 11),  -- 模型分组
(7, 'USER', 3),   -- 积分系统
(8, 'USER', 12),  -- 我的积分
(9, 'USER', 7);   -- 数据统计

-- ADMIN角色权限（继承USER + 管理权限）
INSERT INTO `role_permissions` (`id`, `role`, `permission_id`) VALUES
-- 继承USER权限
(10, 'ADMIN', 1),   -- 控制台
(11, 'ADMIN', 2),   -- 模型管理
(12, 'ADMIN', 8),   -- 计算任务
(13, 'ADMIN', 9),   -- 模型列表
(14, 'ADMIN', 10),  -- 模型分类
(15, 'ADMIN', 11),  -- 模型分组
(16, 'ADMIN', 3),   -- 积分系统
(17, 'ADMIN', 12),  -- 我的积分
(18, 'ADMIN', 7),   -- 数据统计

-- 管理员特有权限
(19, 'ADMIN', 13);  -- 用户积分

-- ADMIN 赋权团队菜单
INSERT INTO `role_permissions` (`id`, `role`, `permission_id`) VALUES
(300, 'ADMIN', 25),  -- 团队管理
(301, 'ADMIN', 26),  -- 成员管理
-- (302, 'ADMIN', 27),  -- 邀请管理（已下线，移除初始化）
(303, 'ADMIN', 28),  -- 加入申请
(304, 'ADMIN', 29);  -- 我的团队

-- USER 赋权团队菜单（仅“我的团队”）
INSERT INTO `role_permissions` (`id`, `role`, `permission_id`) VALUES
(305, 'USER', 25),  -- 团队管理
(306, 'USER', 29);  -- 我的团队

-- SUPER_ADMIN角色权限（所有权限）
INSERT INTO `role_permissions` (`id`, `role`, `permission_id`)
SELECT ROW_NUMBER() OVER (ORDER BY id) + 25, 'SUPER_ADMIN', id FROM `permissions`;

-- 初始化系统配置（修复版，包含配置分类、状态和排序）
INSERT INTO `system_configs` (`id`, `config_key`, `config_value`, `config_type`, `config_type_code`, `config_category`, `config_category_code`, `description`, `status`, `sort_order`) VALUES
(1, 'user.operation.fail.max.count', '5', 'NUMBER', 'DICT_3.3', 'USER', 'DICT_1.7', '用户操作失败最大次数（通用）', 1, 1),
(2, 'user.operation.lock.duration.minutes', '30', 'NUMBER', 'DICT_3.3', 'USER', 'DICT_1.7', '用户操作锁定时长(分钟，通用)', 1, 2),
(9, 'user.operation.fail.window.minutes', '10', 'NUMBER', 'DICT_3.3', 'USER', 'DICT_1.7', '用户操作失败计数窗口(分钟，通用)', 1, 3),
(3, 'config.cache.ttl', '3600', 'NUMBER', 'DICT_3.3', 'SYSTEM', 'DICT_1.8', '缓存默认过期时间(秒)', 1, 4),
(4, 'user.register.enabled', 'true', 'BOOLEAN', 'DICT_3.4', 'USER', 'DICT_1.7', '是否开启用户注册功能', 1, 3),
(5, 'user.register.reward.enabled', 'true', 'BOOLEAN', 'DICT_3.4', 'USER', 'DICT_1.7', '是否开启新用户注册奖励', 1, 4),
(6, 'user.referral.reward.enabled', 'true', 'BOOLEAN', 'DICT_3.4', 'USER', 'DICT_1.7', '是否开启引荐奖励', 1, 5),
(7, 'referral.base.url', 'http://localhost:8081/register?refCode=', 'STRING', 'DICT_3.2', 'USER', 'DICT_1.7', '引荐码基础链接，用于生成完整的引荐链接', 1, 6),
(21, 'team.invite.base.url', 'http://localhost:8081/register?teamCode=', 'STRING', 'DICT_3.2', 'SYSTEM', 'DICT_1.8', '团队邀请基础链接（拼接团队码或邀请token）', 1, 21),
(10, 'notify.email.config', '{"enabled":false,"smtp":{"host":"","port":0,"username":"","password":"","from":""},"rateLimit":{"perMinute":60}}', 'JSON', 'DICT_3.5', 'SYSTEM', 'DICT_1.8', '邮件通知通道配置（JSON）', 1, 20);

-- （移除）默认积分结算模式系统级配置，改为团队级配置（见 team_configs）

-- 权限同步特性开关（默认关闭；通过系统配置启用）
INSERT INTO `system_configs` (`id`, `config_key`, `config_value`, `config_type`, `config_type_code`, `config_category`, `config_category_code`, `description`, `status`, `sort_order`) VALUES
(8, 'permission.sync.role-contribution.enabled', 'false', 'BOOLEAN', 'DICT_3.4', 'SYSTEM', 'DICT_1.8', '角色权限变更后是否同步构建用户权限贡献并在读取时合并', 1, 10);

-- 子账号迁移特性开关（默认关闭；执行一次性迁移时开启）
INSERT INTO `system_configs` (`id`, `config_key`, `config_value`, `config_type`, `config_type_code`, `config_category`, `config_category_code`, `description`, `status`, `sort_order`) VALUES
(11, 'migration.subaccount.team.enabled', 'false', 'BOOLEAN', 'DICT_3.4', 'SYSTEM', 'DICT_1.8', '是否启用子账号迁移为团队的批处理开关（一次性）', 1, 15);

-- 其他系统级配置（补齐代码引用的默认项）
INSERT INTO `system_configs` (`id`, `config_key`, `config_value`, `config_type`, `config_type_code`, `config_category`, `config_category_code`, `description`, `status`, `sort_order`) VALUES
(12, 'user.default.password', 'Abc12345', 'STRING', 'DICT_3.2', 'USER', 'DICT_1.7', '默认用户密码（用于重置或初始化）', 1, 7);

-- 移除：默认积分结算模式系统级配置（已下线团队结算）

INSERT INTO `system_configs` (`id`, `config_key`, `config_value`, `config_type`, `config_type_code`, `config_category`, `config_category_code`, `description`, `status`, `sort_order`) VALUES
(14, 'team.invite.rate.per.minute', '10', 'NUMBER', 'DICT_3.3', 'SYSTEM', 'DICT_1.8', '创建团队邀请-每用户每分钟最大次数', 1, 60),
(15, 'team.join.rate.per.minute', '6', 'NUMBER', 'DICT_3.3', 'SYSTEM', 'DICT_1.8', '提交加入申请-每用户每分钟最大次数', 1, 61),
(16, 'team.invite.idem.ttl.seconds', '30', 'NUMBER', 'DICT_3.3', 'SYSTEM', 'DICT_1.8', '团队邀请创建幂等键TTL（秒）', 1, 62),
(17, 'team.join.idem.ttl.seconds', '30', 'NUMBER', 'DICT_3.3', 'SYSTEM', 'DICT_1.8', '团队加入申请幂等键TTL（秒）', 1, 63);
-- 移除：团队积分消费幂等TTL（已下线团队结算）

INSERT INTO `system_configs` (`id`, `config_key`, `config_value`, `config_type`, `config_type_code`, `config_category`, `config_category_code`, `description`, `status`, `sort_order`) VALUES
(19, 'system.subaccount.api.enabled', 'false', 'BOOLEAN', 'DICT_3.4', 'SYSTEM', 'DICT_1.8', '是否启用旧的子账号API（默认false，逐步下线）', 1, 72);

-- 初始化用户账号
-- 密码: 1234.com (BCrypt加密后的值)
INSERT INTO `users` (`id`, `username`, `email`, `password`, `nickname`, `role`, `parent_user_id`, `referral_code`, `create_time`) VALUES
(1, 'admin', 'admin@medical-platform.com', '$2a$10$.Dyifidc2399Nbdpql4MsOc3HUSii4nfIbDd1dmwEwyl8FR6wShoy', '系统管理员', 'SUPER_ADMIN', NULL, 'SUPER888', NOW()),
(2, 'manager', 'manager@medical-platform.com', '$2a$10$.Dyifidc2399Nbdpql4MsOc3HUSii4nfIbDd1dmwEwyl8FR6wShoy', '平台管理员', 'ADMIN', NULL, 'ADMN6666', NOW()),
(3, 'user', 'user@medical-platform.com', '$2a$10$.Dyifidc2399Nbdpql4MsOc3HUSii4nfIbDd1dmwEwyl8FR6wShoy', '普通用户', 'USER', NULL, 'USER999', NOW()),
-- 管理员子用户示例（parent_user_id 指向 manager=2）
(4, 'teamuser1', 'team1@medical-platform.com', '$2a$10$.Dyifidc2399Nbdpql4MsOc3HUSii4nfIbDd1dmwEwyl8FR6wShoy', '团队用户一', 'USER', 2, 'TEAM001', NOW()),
(5, 'teamuser2', 'team2@medical-platform.com', '$2a$10$.Dyifidc2399Nbdpql4MsOc3HUSii4nfIbDd1dmwEwyl8FR6wShoy', '团队用户二', 'USER', 2, 'TEAM002', NOW());

-- 初始化用户通知渠道偏好（默认：仅开启站内，邮件/短信关闭）
INSERT INTO `user_notify_channel_prefs` (`id`, `user_id`, `channel_code`, `enabled`, `do_not_disturb`, `create_time`)
SELECT (400000 + u.id * 10 + 1) AS id, u.id AS user_id, 'inbox' AS channel_code, 1 AS enabled, NULL AS do_not_disturb, NOW() AS create_time
FROM `users` u WHERE u.is_deleted = 0
ON DUPLICATE KEY UPDATE `enabled` = VALUES(`enabled`), `update_time` = NOW();

INSERT INTO `user_notify_channel_prefs` (`id`, `user_id`, `channel_code`, `enabled`, `do_not_disturb`, `create_time`)
SELECT (400000 + u.id * 10 + 2) AS id, u.id AS user_id, 'email' AS channel_code, 0 AS enabled, NULL AS do_not_disturb, NOW() AS create_time
FROM `users` u WHERE u.is_deleted = 0
ON DUPLICATE KEY UPDATE `enabled` = VALUES(`enabled`), `update_time` = NOW();

INSERT INTO `user_notify_channel_prefs` (`id`, `user_id`, `channel_code`, `enabled`, `do_not_disturb`, `create_time`)
SELECT (400000 + u.id * 10 + 3) AS id, u.id AS user_id, 'sms' AS channel_code, 0 AS enabled, NULL AS do_not_disturb, NOW() AS create_time
FROM `users` u WHERE u.is_deleted = 0
ON DUPLICATE KEY UPDATE `enabled` = VALUES(`enabled`), `update_time` = NOW();

-- 初始化团队与成员（不为 SUPER_ADMIN 建团队；为 ADMIN 创建默认团队并迁移其子用户）
INSERT INTO `teams` (`id`,`team_name`,`team_code`,`description`,`owner_user_id`,`status`,`is_deleted`,`create_time`,`update_time`)
SELECT (200000 + u.id) AS id,
       CONCAT('初始测试团队') AS team_name,
       CONCAT('TC', LPAD(u.id, 6, '0')) AS team_code,
       CONCAT('默认团队-由用户 ', u.username, ' 创建') AS description,
       u.id AS owner_user_id, 1, 0, NOW(), NOW()
FROM `users` u WHERE u.role='ADMIN' AND u.is_deleted=0
ON DUPLICATE KEY UPDATE team_name=VALUES(team_name), update_time=NOW();

-- 将管理员本人加入团队为 OWNER
INSERT INTO `team_members` (`id`,`team_id`,`user_id`,`team_role`,`status`,`is_deleted`,`create_time`,`update_time`)
SELECT (300000 + u.id) AS id, (200000 + u.id) AS team_id, u.id AS user_id, 'OWNER', 1, 0, NOW(), NOW()
FROM `users` u WHERE u.role='ADMIN' AND u.is_deleted=0
ON DUPLICATE KEY UPDATE team_role='OWNER', status=1, update_time=NOW();

-- 将管理员的历史子用户迁移为该团队的 MEMBER
INSERT INTO `team_members` (`id`,`team_id`,`user_id`,`team_role`,`status`,`is_deleted`,`create_time`,`update_time`)
SELECT (300000 + s.id) AS id, (200000 + a.id) AS team_id, s.id AS user_id, 'MEMBER', 1, 0, NOW(), NOW()
FROM `users` s
JOIN `users` a ON s.parent_user_id=a.id AND a.role='ADMIN'
WHERE s.is_deleted=0
ON DUPLICATE KEY UPDATE team_role='MEMBER', status=1, update_time=NOW();

-- 为现有团队创建默认配置
-- 移除：团队结算模式团队级配置（已下线团队结算）

-- 为现有团队创建“加入是否需审批”配置（默认：不需要审批）
INSERT INTO `team_configs` (`id`, `team_id`, `config_key`, `config_value`, `config_type`, `description`, `create_time`)
SELECT
  (210000 + t.id) AS id,
  t.id AS team_id,
  'team.join.requireApproval' AS config_key,
  'false' AS config_value,
  'BOOLEAN' AS config_type,
  '加入团队是否需要审批' AS description,
  NOW() AS create_time
FROM `teams` t
WHERE t.is_deleted = 0
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 为现有团队创建积分账户
-- 移除：团队积分账户初始化（team_credits）


-- 初始化用户-权限关联（基于新逻辑：仅 ALLOW）
-- SUPER_ADMIN 授予全部启用权限
INSERT INTO `user_permissions` (`id`, `user_id`, `permission_id`, `grant_type`, `create_time`)
SELECT (10000 + p.id), 1 AS user_id, p.id AS permission_id, 'ALLOW' AS grant_type, NOW()
FROM `permissions` p
WHERE p.status = 1;

-- ADMIN 角色：授予其角色的所有权限
INSERT INTO `user_permissions` (`id`, `user_id`, `permission_id`, `grant_type`, `create_time`)
SELECT (20000 + rp.permission_id), 2 AS user_id, rp.permission_id, 'ALLOW', NOW()
FROM `role_permissions` rp
WHERE rp.role = 'ADMIN';

-- USER 角色：授予其角色的所有权限（包含 teamuser1, teamuser2）
INSERT INTO `user_permissions` (`id`, `user_id`, `permission_id`, `grant_type`, `create_time`)
SELECT (30000 + rp.permission_id), 3 AS user_id, rp.permission_id, 'ALLOW', NOW()
FROM `role_permissions` rp
WHERE rp.role = 'USER';

INSERT INTO `user_permissions` (`id`, `user_id`, `permission_id`, `grant_type`, `create_time`)
SELECT (31000 + rp.permission_id), 4 AS user_id, rp.permission_id, 'ALLOW', NOW()
FROM `role_permissions` rp
WHERE rp.role = 'USER';

INSERT INTO `user_permissions` (`id`, `user_id`, `permission_id`, `grant_type`, `create_time`)
SELECT (32000 + rp.permission_id), 5 AS user_id, rp.permission_id, 'ALLOW', NOW()
FROM `role_permissions` rp
WHERE rp.role = 'USER';

-- 初始化用户积分账户
INSERT INTO `user_credits` (`id`, `user_id`, `credit_type_code`, `balance`, `total_earned`, `create_time`) VALUES
-- 超级管理员积分账户
(1, 1, 'NORMAL', 10000.00, 10000.00, NOW()),
(2, 1, 'PREMIUM', 10000.00, 10000.00, NOW()),
-- 管理员积分账户
(3, 2, 'NORMAL', 0.00, 0.00, NOW()),
(4, 2, 'PREMIUM', 0.00, 0.00, NOW()),
-- 普通用户积分账户
(5, 3, 'NORMAL', 0.00, 0.00, NOW()),
(6, 3, 'PREMIUM', 0.00, 0.00, NOW()),
-- 子用户积分账户
(7, 4, 'NORMAL', 0.00, 0.00, NOW()),
(8, 4, 'PREMIUM', 0.00, 0.00, NOW()),
(9, 5, 'NORMAL', 0.00, 0.00, NOW()),
(10, 5, 'PREMIUM', 0.00, 0.00, NOW());

-- 初始化用户积分交易记录
INSERT INTO `credit_transactions` (`id`, `user_id`, `credit_type_code`, `transaction_type`, `amount`, `balance_before`, `balance_after`, `description`, `create_time`) VALUES
-- 超级管理员积分交易记录
(1, 1, 'NORMAL', 'REGISTER', 10000.00, 0.00, 10000.00, '系统初始化积分', NOW()),
(2, 1, 'PREMIUM', 'REGISTER', 10000.00, 0.00, 10000.00, '系统初始化积分', NOW());

-- 初始化字典数据（修复版，使用统一的编码规则）
-- 1. 配置类型字典
INSERT INTO `system_dict` (`id`, `dict_code`, `dict_name`, `dict_label`, `description`, `parent_id`, `level`, `path`, `module`, `status`, `sort_order`) VALUES
(1, 'DICT_3', '数据类型', '数据类型', '系统配置的数据类型', 0, 1, '3', 'SYSTEM', 1, 1);

INSERT INTO `system_dict` (`id`, `dict_code`, `dict_name`, `dict_label`, `description`, `parent_id`, `level`, `path`, `module`, `status`, `sort_order`) VALUES
(2, 'DICT_3.2', '字符串类型', '字符串', '字符串数据类型', 1, 2, '3.2', 'SYSTEM', 1, 1),
(3, 'DICT_3.3', '数字类型', '数字', '数字数据类型', 1, 2, '3.3', 'SYSTEM', 1, 2),
(4, 'DICT_3.4', '布尔类型', '布尔值', '布尔数据类型', 1, 2, '3.4', 'SYSTEM', 1, 3),
(5, 'DICT_3.5', 'JSON类型', 'JSON对象', 'JSON数据类型', 1, 2, '3.5', 'SYSTEM', 1, 4);

-- 2. 配置分类字典
INSERT INTO `system_dict` (`id`, `dict_code`, `dict_name`, `dict_label`, `description`, `parent_id`, `level`, `path`, `module`, `status`, `sort_order`) VALUES
(6, 'DICT_1', '配置分类', '配置分类', '系统配置的分类管理', 0, 1, '1', 'SYSTEM', 1, 2);

INSERT INTO `system_dict` (`id`, `dict_code`, `dict_name`, `dict_label`, `description`, `parent_id`, `level`, `path`, `module`, `status`, `sort_order`) VALUES
(7, 'DICT_1.7', '用户配置分类', '用户配置', '用户相关的系统配置', 6, 2, '1.7', 'SYSTEM', 1, 1),
(8, 'DICT_1.8', '系统配置分类', '系统配置', '系统核心配置', 6, 2, '1.8', 'SYSTEM', 1, 2),
(9, 'DICT_1.9', '安全配置分类', '安全配置', '安全相关的系统配置', 6, 2, '1.9', 'SYSTEM', 1, 3),
(10, 'DICT_1.10', '缓存配置分类', '缓存配置', '缓存相关的系统配置', 6, 2, '1.10', 'SYSTEM', 1, 4),
(11, 'DICT_1.11', '文件配置分类', '文件配置', '文件相关的系统配置', 6, 2, '1.11', 'SYSTEM', 1, 5),
(12, 'DICT_1.12', '任务配置分类', '任务配置', '任务相关的系统配置', 6, 2, '1.12', 'SYSTEM', 1, 6);

-- 3. 消息通知字典（新增：按截图中的“消息通知配置”）
-- 顶级：DICT_4 - 消息通知
INSERT INTO `system_dict` (`id`, `dict_code`, `dict_name`, `dict_label`, `description`, `parent_id`, `level`, `path`, `module`, `status`, `sort_order`) VALUES
(100, 'DICT_4', '消息通知', '消息通知', '平台消息通知配置（定义推送渠道与消息类型）', 0, 1, '4', 'SYSTEM', 1, 1);

-- 二级：DICT_4.1 - 推送渠道；DICT_4.2 - 消息类型
INSERT INTO `system_dict` (`id`, `dict_code`, `dict_name`, `dict_label`, `description`, `parent_id`, `level`, `path`, `module`, `status`, `sort_order`) VALUES
(101, 'DICT_4.1', '推送渠道', '推送渠道', '消息投递通道（如：站内、邮件、短信）', 100, 2, '4.1', 'SYSTEM', 1, 1),
(102, 'DICT_4.2', '消息类型', '消息类型', '按业务维度划分的消息类别（系统/任务/积分/营销）', 100, 2, '4.2', 'SYSTEM', 1, 2);

-- 三级：渠道子项（短信保留占位，默认禁用由业务控制）
INSERT INTO `system_dict` (`id`, `dict_code`, `dict_name`, `dict_label`, `description`, `parent_id`, `level`, `path`, `module`, `status`, `sort_order`) VALUES
(111, 'DICT_4.1.1', 'inbox', '站内', '站内通知：平台消息中心接收', 101, 3, '4.1.1', 'SYSTEM', 1, 1),
(112, 'DICT_4.1.2', 'email', '邮件', '邮件通知：发送至绑定邮箱，适合重要消息留痕', 101, 3, '4.1.2', 'SYSTEM', 1, 2),
(113, 'DICT_4.1.3', 'sms', '短信', '短信通知：发送至绑定手机号', 101, 3, '4.1.3', 'SYSTEM', 1, 3);

-- 三级：类型子项（按截图：积分通知、营销通知、系统通知、任务通知）
INSERT INTO `system_dict` (`id`, `dict_code`, `dict_name`, `dict_label`, `description`, `parent_id`, `level`, `path`, `module`, `status`, `sort_order`) VALUES
(121, 'DICT_4.2.1', 'system', '系统消息', '系统相关通知：账号安全、系统公告、配置变更等', 102, 3, '4.2.1', 'SYSTEM', 1, 1),
(122, 'DICT_4.2.2', 'task', '任务消息', '任务相关通知：AI计算/模型任务的排队、进度与完成等', 102, 3, '4.2.2', 'SYSTEM', 1, 2),
(123, 'DICT_4.2.3', 'credit', '积分变动', '积分账户变动：发放、消费、退款与余额提醒', 102, 3, '4.2.3', 'SYSTEM', 1, 3),
(124, 'DICT_4.2.4', 'marketing', '营销消息', '活动与营销通知：优惠、福利、运营活动等', 102, 3, '4.2.4', 'SYSTEM', 1, 4);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ===================================================================
-- 数据库初始化完成
-- ===================================================================