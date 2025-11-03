package com.okbug.platform.config.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 初始化团队相关表的索引与唯一约束（幂等执行）。
 *
 * 说明：
 * - 通过 information_schema 判断索引是否存在，避免重复创建
 * - 仅创建与查询强相关的索引/唯一键，保持最小必要集合
 * - 需要数据库账号具备相应 DDL 权限
 */
@Component
public class TeamSchemaInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TeamSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public TeamSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            String schema = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
            if (schema == null || schema.isEmpty()) {
                logger.warn("Skip TeamSchemaInitializer: schema is empty");
                return;
            }

            // ============ teams ============
            ensureIndexExists(schema, "teams", "uk_teams_team_code",
                    "CREATE UNIQUE INDEX uk_teams_team_code ON teams(team_code)");
            // 允许团队名称重复：删除历史上的唯一索引并创建普通索引
            dropIndexIfExists(schema, "teams", "uk_teams_team_name");
            ensureIndexExists(schema, "teams", "idx_teams_team_name",
                    "CREATE INDEX idx_teams_team_name ON teams(team_name)");
            ensureIndexExists(schema, "teams", "idx_teams_owner_user_id",
                    "CREATE INDEX idx_teams_owner_user_id ON teams(owner_user_id)");

            // ============ team_members ============
            ensureIndexExists(schema, "team_members", "uk_team_members_team_user",
                    "CREATE UNIQUE INDEX uk_team_members_team_user ON team_members(team_id, user_id)");
            ensureIndexExists(schema, "team_members", "idx_team_members_team_id",
                    "CREATE INDEX idx_team_members_team_id ON team_members(team_id)");
            ensureIndexExists(schema, "team_members", "idx_team_members_user_id",
                    "CREATE INDEX idx_team_members_user_id ON team_members(user_id)");
            ensureIndexExists(schema, "team_members", "idx_team_members_status",
                    "CREATE INDEX idx_team_members_status ON team_members(status)");

            // ============ team_invitations ============
            ensureIndexExists(schema, "team_invitations", "uk_team_invitations_token",
                    "CREATE UNIQUE INDEX uk_team_invitations_token ON team_invitations(invitation_token)");
            ensureIndexExists(schema, "team_invitations", "idx_team_invitations_team_status_expire",
                    "CREATE INDEX idx_team_invitations_team_status_expire ON team_invitations(team_id, status, expire_time)");
            ensureIndexExists(schema, "team_invitations", "idx_team_invitations_invited_user",
                    "CREATE INDEX idx_team_invitations_invited_user ON team_invitations(invited_user_id)");

            // ============ team_join_requests ============
            ensureIndexExists(schema, "team_join_requests", "idx_join_requests_team_status",
                    "CREATE INDEX idx_join_requests_team_status ON team_join_requests(team_id, status)");
            ensureIndexExists(schema, "team_join_requests", "idx_join_requests_user",
                    "CREATE INDEX idx_join_requests_user ON team_join_requests(user_id)");

            

            logger.info("TeamSchemaInitializer completed for schema: {}", schema);
        } catch (Exception e) {
            logger.warn("TeamSchemaInitializer skipped due to error: {}", e.getMessage());
        }
    }

    private void ensureIndexExists(String schema, String tableName, String indexName, String createIndexSql) {
        String existsSql = "SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema = ? AND table_name = ? AND index_name = ?";
        Integer count = jdbcTemplate.queryForObject(existsSql, Integer.class, schema, tableName, indexName);
        if (Objects.equals(count, 0)) {
            try {
                jdbcTemplate.execute(createIndexSql);
                logger.info("Created index {} on {}", indexName, tableName);
            } catch (Exception e) {
                logger.warn("Create index {} on {} failed: {}", indexName, tableName, e.getMessage());
            }
        }
    }

    private void dropIndexIfExists(String schema, String tableName, String indexName) {
        String existsSql = "SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema = ? AND table_name = ? AND index_name = ?";
        Integer count = jdbcTemplate.queryForObject(existsSql, Integer.class, schema, tableName, indexName);
        if (!Objects.equals(count, 0)) {
            try {
                jdbcTemplate.execute("DROP INDEX " + indexName + " ON " + tableName);
                logger.info("Dropped index {} on {}", indexName, tableName);
            } catch (Exception e) {
                logger.warn("Drop index {} on {} failed: {}", indexName, tableName, e.getMessage());
            }
        }
    }
}


