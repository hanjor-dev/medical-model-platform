/**
 * 操作日志数据访问层
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:45:00
 */
package com.okbug.platform.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.entity.auth.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    
    /**
     * 分页查询操作日志（支持多条件筛选）
     */
    @Select("<script>" +
            "SELECT * FROM operation_logs " +
            "WHERE 1=1 " +
            "<if test='userId != null'> AND user_id = #{userId} </if>" +
            "<if test='username != null and username != \"\"'> AND username LIKE CONCAT('%', #{username}, '%') </if>" +
            "<if test='operationModule != null and operationModule != \"\"'> AND operation_module = #{operationModule} </if>" +
            "<if test='operationStatus != null'> AND operation_status = #{operationStatus} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (operation_desc LIKE CONCAT('%', #{keyword}, '%') OR request_url LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "<if test='startTime != null'> AND operation_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND operation_time &lt;= #{endTime} </if>" +
            "<choose>" +
            "  <when test='orderBy != null and orderBy != \"\"'> ORDER BY ${orderBy} </when>" +
            "  <otherwise> ORDER BY operation_time DESC </otherwise>" +
            "</choose>" +
            "</script>")
    IPage<OperationLog> selectPageWithConditions(Page<OperationLog> page,
                                                @Param("userId") Long userId,
                                                @Param("username") String username,
                                                @Param("operationModule") String operationModule,
                                                @Param("operationStatus") Integer operationStatus,
                                                @Param("keyword") String keyword,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime,
                                                @Param("orderBy") String orderBy);

    /**
     * 分页查询操作日志（通过用户ID/用户名集合进行过滤）
     */
    @Select("<script>" +
            "SELECT * FROM operation_logs " +
            "WHERE 1=1 " +
            "<if test='userIds != null and userIds.size() &gt; 0 or usernames != null and usernames.size() &gt; 0'>" +
            "  AND ( 1=0 " +
            "    <if test='userIds != null and userIds.size() &gt; 0'> OR user_id IN " +
            "      <foreach collection='userIds' item='uid' open='(' separator=',' close=')'> #{uid} </foreach> " +
            "    </if>" +
            "    <if test='usernames != null and usernames.size() &gt; 0'> OR username IN " +
            "      <foreach collection='usernames' item='uname' open='(' separator=',' close=')'> #{uname} </foreach> " +
            "    </if>" +
            "  )" +
            "</if>" +
            "<if test='operationModule != null and operationModule != \"\"'> AND operation_module = #{operationModule} </if>" +
            "<if test='operationStatus != null'> AND operation_status = #{operationStatus} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (operation_desc LIKE CONCAT('%', #{keyword}, '%') OR request_url LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "<if test='startTime != null'> AND operation_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND operation_time &lt;= #{endTime} </if>" +
            "<choose>" +
            "  <when test='orderBy != null and orderBy != \"\"'> ORDER BY ${orderBy} </when>" +
            "  <otherwise> ORDER BY operation_time DESC </otherwise>" +
            "</choose>" +
            "</script>")
    IPage<OperationLog> selectPageWithUserFilters(Page<OperationLog> page,
                                                 @Param("userIds") List<Long> userIds,
                                                 @Param("usernames") List<String> usernames,
                                                 @Param("operationModule") String operationModule,
                                                 @Param("operationStatus") Integer operationStatus,
                                                 @Param("keyword") String keyword,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("orderBy") String orderBy);
    
    
    
    /**
     * 批量删除过期日志
     */
    @Delete("DELETE FROM operation_logs WHERE operation_time < #{expireTime}")
    int deleteExpiredLogs(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 批量删除过期日志（限定用户范围）
     */
    @Delete("<script>" +
            "DELETE FROM operation_logs " +
            "WHERE operation_time &lt; #{expireTime} " +
            "AND user_id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'> #{id} </foreach>" +
            "</script>")
    int deleteExpiredLogsByUserIds(@Param("expireTime") LocalDateTime expireTime,
                                   @Param("userIds") List<Long> userIds);

    
} 
