/**
 * 用户日志服务实现类
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 11:40:00
 */
package com.okbug.platform.service.log.impl;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.dto.log.LogQueryRequest;
import com.okbug.platform.dto.log.LogResponse;
import com.okbug.platform.entity.auth.OperationLog;
// import com.okbug.platform.entity.auth.UserLoginLog;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.mapper.auth.OperationLogMapper;
// import com.okbug.platform.mapper.auth.UserLoginLogMapper;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.service.log.UserLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLogServiceImpl implements UserLogService {
    
    private final OperationLogMapper operationLogMapper;
    private final UserMapper userMapper;
    
    @Override
    /**
     * 查询日志列表：仅返回操作日志，支持模块/状态/关键字/时间范围与用户筛选。
     */
    public IPage<LogResponse> queryLogs(LogQueryRequest request) {
        // 处理快速时间范围
        processQuickTimeRange(request);

        // 日志已统一到操作日志表，直接查询操作日志（包含数据范围与排序）
        return queryOperationLogs(request);
    }
    
    @Override
    /**
     * 获取日志详情。
     *
     * @param logId 日志ID
     * @return 日志响应
     * @throws ServiceException 当日志不存在时抛出 {@link ErrorCode#LOG_NOT_FOUND}
     */
    public LogResponse getLogDetail(Long logId) {
            OperationLog operationLog = operationLogMapper.selectById(logId);
            if (operationLog == null) {
                throw new ServiceException(ErrorCode.LOG_NOT_FOUND, "操作日志不存在");
            }
            return convertToLogResponse(operationLog);
    }
    
    
    
    // 统一后不再单独写入登录日志
    
    @Override
    /**
     * 记录操作日志（同步）。失败仅记录错误，不影响主流程。
     */
    public void recordOperationLog(OperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
            log.debug("记录操作日志成功: userId={}, module={}, type={}", 
                     operationLog.getUserId(), operationLog.getOperationModule(), operationLog.getOperationType());
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
            // 日志记录失败不应影响主业务，只记录错误日志
        }
    }
    
    @Override
    @Async
    /**
     * 记录操作日志（异步）。
     */
    public void recordOperationLogAsync(OperationLog operationLog) {
        recordOperationLog(operationLog);
    }
    
    @Override
    /**
     * 清理过期日志。
     *
     * @param expireTime 过期阈值（早于该时间的日志将被清理）
     * @return 删除的操作日志条数
     */
    public int cleanExpiredLogs(LocalDateTime expireTime) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        List<Long> accessibleUserIds = computeAccessibleUserIds(currentUserId);
        int deletedOperationLogs;
        if (accessibleUserIds == null) {
            // 超级管理员：不限用户范围，清理所有过期日志
            deletedOperationLogs = operationLogMapper.deleteExpiredLogs(expireTime);
        } else if (accessibleUserIds.isEmpty()) {
            deletedOperationLogs = 0;
        } else {
            // 普通用户或管理员：仅清理可见用户范围内的过期日志
            deletedOperationLogs = operationLogMapper.deleteExpiredLogsByUserIds(expireTime, accessibleUserIds);
        }
        log.info("清理过期日志完成: 操作日志{}条", deletedOperationLogs);
        return deletedOperationLogs;
    }
    
    @Override
    /**
     * 导出日志为 CSV（UTF-8 BOM），最多导出 20000 条。
     *
     * @param request   查询条件
     * @param exportType 导出类型（预留参数）
     * @return CSV 字节数组
     */
    public byte[] exportLogs(LogQueryRequest request, String exportType) {
        // 复用查询构造，但不分页，限制最大导出条数
        final int MAX_EXPORT = 20000;
        processQuickTimeRange(request);
        String orderBy = buildOrderBy(request);

        // 为了导出，单独查询不分页
        List<OperationLog> records;
        try {
            Long currentUserId = safeGetCurrentUserId();
            List<Long> accessibleUserIds = computeAccessibleUserIds(currentUserId);

            // 基于查询条件加载全部（加上合理上限）
            Page<OperationLog> page = new Page<>(1, MAX_EXPORT);
            IPage<OperationLog> p;
            boolean needUserFilter = false;
            List<Long> matchedUserIds = null;
            List<String> matchedUsernames = null;
            if (org.springframework.util.StringUtils.hasText(request.getUsername())) {
                List<User> userCandidates = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .like(User::getUsername, request.getUsername())
                    .or().like(User::getNickname, request.getUsername())
                    .or().like(User::getEmail, request.getUsername()));
                matchedUserIds = userCandidates.isEmpty() ? Collections.singletonList(-1L)
                        : userCandidates.stream().map(User::getId).collect(Collectors.toList());
            }
            if (accessibleUserIds != null) {
                if (matchedUserIds != null) {
                    matchedUserIds = matchedUserIds.stream().filter(accessibleUserIds::contains).collect(Collectors.toList());
                    if (matchedUserIds.isEmpty() && org.springframework.util.StringUtils.hasText(request.getUsername())) {
                        matchedUserIds = Collections.singletonList(-1L);
                    }
                } else {
                    matchedUserIds = new ArrayList<>(accessibleUserIds);
                }
                matchedUsernames = null;
                needUserFilter = true;
            } else if (matchedUserIds != null) {
                needUserFilter = true;
            }

            if (needUserFilter) {
                p = operationLogMapper.selectPageWithUserFilters(
                    page,
                    matchedUserIds,
                    matchedUsernames,
                    request.getOperationModule(),
                    request.getStatus(),
                    request.getKeyword(),
                    request.getStartTime(),
                    request.getEndTime(),
                    orderBy
                );
            } else {
                p = operationLogMapper.selectPageWithConditions(
                    page,
                    request.getUserId(),
                    request.getUsername(),
                    request.getOperationModule(),
                    request.getStatus(),
                    request.getKeyword(),
                    request.getStartTime(),
                    request.getEndTime(),
                    orderBy
                );
            }
            records = p.getRecords();
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.LOG_EXPORT_FAILED, "查询导出数据失败: " + e.getMessage());
        }

        try {
            // 以UTF-8 BOM的CSV，Excel可直接打开
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                out.write(new byte[]{(byte)0xEF,(byte)0xBB,(byte)0xBF});
                String[] headers = {"日志ID","用户ID","用户名","昵称","模块","类型","描述","状态","IP","地点","方法","URL","耗时(ms)","时间"};
                writer.write(String.join(",", headers));
                writer.write("\n");

                Map<String, String> usernameToNickname = loadNicknamesByLogs(
                    records.stream().map(OperationLog::getUsername).filter(Objects::nonNull).distinct().collect(Collectors.toList()),
                    records.stream().map(OperationLog::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList())
                );

                for (OperationLog logEntity : records) {
                    String[] row = new String[] {
                        safe(logEntity.getId()),
                        safe(logEntity.getUserId()),
                        safe(logEntity.getUsername()),
                        escapeCSV(usernameToNickname.getOrDefault(logEntity.getUsername(), "")),
                        safe(logEntity.getOperationModule()),
                        safe(logEntity.getOperationType()),
                        escapeCSV(logEntity.getOperationDesc()),
                        logEntity.getOperationStatus() != null && logEntity.getOperationStatus() == 1 ? "成功" : "失败",
                        safe(logEntity.getOperationIp()),
                        escapeCSV(logEntity.getOperationLocation()),
                        safe(logEntity.getRequestMethod()),
                        escapeCSV(logEntity.getRequestUrl()),
                        safe(logEntity.getCostTime()),
                        logEntity.getOperationTime() == null ? "" : logEntity.getOperationTime().toString().replace('T', ' ')
                    };
                    writer.write(String.join(",", row));
                    writer.write("\n");
                }
                writer.flush();
                return out.toByteArray();
            }
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.LOG_EXPORT_FAILED, "导出文件生成失败: " + e.getMessage());
        }
    }

    private String safe(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    @Override
    public List<Map<String, String>> getOperationModuleOptions() {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (OperationModule m : OperationModule.values()) {
            Map<String, String> map = new HashMap<String, String>(2);
            map.put("value", m.name());
            map.put("label", m.getLabel());
            list.add(map);
        }
        return list;
    }
    
    
    // ================ 私有方法 ================
    
    /**
     * 处理快速时间范围。
     */
    private void processQuickTimeRange(LogQueryRequest request) {
        if (!StringUtils.hasText(request.getQuickTimeRange())) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = null;
        LocalDateTime endTime = now;
        
        switch (request.getQuickTimeRange()) {
            case LogQueryRequest.QUICK_TIME_TODAY:
                startTime = now.with(LocalTime.MIN);
                break;
            case LogQueryRequest.QUICK_TIME_YESTERDAY:
                startTime = now.minusDays(1).with(LocalTime.MIN);
                endTime = now.minusDays(1).with(LocalTime.MAX);
                break;
            case LogQueryRequest.QUICK_TIME_WEEK:
                startTime = now.minusDays(7);
                break;
            case LogQueryRequest.QUICK_TIME_MONTH:
                startTime = now.minusDays(30);
                break;
        }
        
        if (startTime != null) {
            request.setStartTime(startTime);
            request.setEndTime(endTime);
        }
    }
    
    /**
     * 查询操作日志。
     */
    private IPage<LogResponse> queryOperationLogs(LogQueryRequest request) {
        Page<OperationLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        String orderBy = buildOrderBy(request);

        // 计算数据范围（超级管理员=不限制；管理员=自己+子用户；普通用户=自己）
        Long currentUserId = safeGetCurrentUserId();
        List<Long> accessibleUserIds = computeAccessibleUserIds(currentUserId);

        // 处理基于用户名/关键字的用户匹配
        List<Long> matchedUserIds = null;
        List<String> matchedUsernames = null;
        if (StringUtils.hasText(request.getUsername())) {
            // "用户"筛选：支持账号/昵称/邮箱模糊匹配 → 转换为 user_id 过滤
            List<User> userCandidates = userMapper.selectList(new LambdaQueryWrapper<User>()
                .like(User::getUsername, request.getUsername())
                .or().like(User::getNickname, request.getUsername())
                .or().like(User::getEmail, request.getUsername()));
            if (!userCandidates.isEmpty()) {
                matchedUserIds = userCandidates.stream().map(User::getId).collect(Collectors.toList());
            } else {
                matchedUserIds = Collections.singletonList(-1L); // 无匹配，保证返回空
            }
        }
        // 若仍希望通过关键字联动用户模糊匹配，可保留以下逻辑与日志内容关键字并行
        // 这里不设置 matchedUsernames，以统一通过 userIds 过滤避免越权

        // 应用数据范围到匹配用户集合
        if (accessibleUserIds != null) {
            if (matchedUserIds != null) {
                matchedUserIds = matchedUserIds.stream().filter(accessibleUserIds::contains).collect(Collectors.toList());
                // 若因越权过滤后无可见用户，并且前端指定了用户名筛选，则强制返回空集
                if (matchedUserIds.isEmpty() && StringUtils.hasText(request.getUsername())) {
                    matchedUserIds = Collections.singletonList(-1L);
                }
            } else {
                // 未指定用户筛选，但有范围限制，则范围即为过滤集合
                matchedUserIds = new ArrayList<>(accessibleUserIds);
            }
            matchedUsernames = null; // 统一用ID进行过滤，避免越权
        }

        IPage<OperationLog> operationLogPage;
        boolean needUserFilter = (accessibleUserIds != null) ||
                (matchedUserIds != null && !matchedUserIds.isEmpty()) ||
                (matchedUsernames != null && !matchedUsernames.isEmpty());
        if (needUserFilter) {
            // 如果无匹配但需要范围限制，用范围ID作为过滤
            List<Long> userIdsForFilter = matchedUserIds;
            // accessibleUserIds 已经合并进 matchedUserIds
            operationLogPage = operationLogMapper.selectPageWithUserFilters(
                page,
                userIdsForFilter,
                matchedUsernames,
                request.getOperationModule(),
                request.getStatus(),
                request.getKeyword(),
                request.getStartTime(),
                request.getEndTime(),
                orderBy
            );
        } else {
            operationLogPage = operationLogMapper.selectPageWithConditions(
                page,
                request.getUserId(),
                request.getUsername(),
                request.getOperationModule(),
                request.getStatus(),
                request.getKeyword(),
                request.getStartTime(),
                request.getEndTime(),
                orderBy
            );
        }

        Map<String, String> usernameToNickname = loadNicknamesByLogs(
            operationLogPage.getRecords().stream().map(OperationLog::getUsername).filter(Objects::nonNull).distinct().collect(Collectors.toList()),
            operationLogPage.getRecords().stream().map(OperationLog::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList())
        );

        IPage<LogResponse> result = new Page<>(operationLogPage.getCurrent(), operationLogPage.getSize(), operationLogPage.getTotal());
        List<LogResponse> responses = operationLogPage.getRecords().stream()
            .map(ol -> {
                LogResponse r = convertToLogResponse(ol);
                r.setUserNickname(usernameToNickname.getOrDefault(r.getUsername(), null));
                return r;
            })
            .collect(Collectors.toList());
        result.setRecords(responses);
        return result;
    }

    private Long safeGetCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    private List<Long> computeAccessibleUserIds(Long currentUserId) {
        if (currentUserId == null) {
            return null; // 匿名或未登录情况下不作范围限制（通常网关会拦截）
        }
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null) {
            return Collections.singletonList(-1L); // 不会匹配任何记录
        }
        if (currentUser.isSuperAdmin()) {
            return null; // 不限制
        }
        if (currentUser.hasAdminRole()) {
            List<User> subs = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getParentUserId, currentUserId));
            List<Long> ids = new ArrayList<>();
            ids.add(currentUserId);
            if (subs != null && !subs.isEmpty()) {
                ids.addAll(subs.stream().map(User::getId).collect(Collectors.toList()));
            }
            return ids;
        }
        return Collections.singletonList(currentUserId);
    }

    private String buildOrderBy(LogQueryRequest request) {
        // 允许排序字段白名单
        Map<String, String> allowed = new HashMap<String, String>(8);
        allowed.put("operation_time", "operation_time");
        allowed.put("operation_status", "operation_status");
        allowed.put("cost_time", "cost_time");
        allowed.put("user_id", "user_id");

        String field = request.getSortField() != null ? request.getSortField().toLowerCase() : "operation_time";
        String column = allowed.getOrDefault(field, "operation_time");
        String order = (request.getSortOrder() != null && request.getSortOrder().equalsIgnoreCase("ASC")) ? "ASC" : "DESC";
        return column + " " + order;
    }
    
    // 统一后不再进行ALL合并查询，直接复用查询方法
    // private IPage<LogResponse> queryAllLogs(LogQueryRequest request) {
    //     // 简化实现：分别查询后合并（实际项目中可考虑使用UNION查询优化性能）
    //     List<LogResponse> allLogs = new ArrayList<>();
        
    //     // 统一按照用户关键字进行过滤（username/nickname/email）
    //     List<User> userCandidates = null;
    //     if (StringUtils.hasText(request.getUsername()) || StringUtils.hasText(request.getKeyword())) {
    //         String kw = StringUtils.hasText(request.getKeyword()) ? request.getKeyword() : request.getUsername();
    //         userCandidates = userMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
    //             .like(User::getUsername, kw)
    //             .or().like(User::getNickname, kw)
    //             .or().like(User::getEmail, kw));
    //     }
    //     Set<Long> candidateIds = userCandidates != null ? userCandidates.stream().map(User::getId).collect(Collectors.toSet()) : Collections.emptySet();
    //     Set<String> candidateUsernames = userCandidates != null ? userCandidates.stream().map(User::getUsername).collect(Collectors.toSet()) : Collections.emptySet();

    //     // 查询登录日志
    //     if (request.getOperationModule() == null || "LOGIN".equals(request.getOperationModule())) {
    //         LambdaQueryWrapper<UserLoginLog> loginWrapper = new LambdaQueryWrapper<>();
    //         buildLoginLogQueryWrapper(loginWrapper, request);
    //         if (!candidateIds.isEmpty()) {
    //             loginWrapper.in(UserLoginLog::getUserId, candidateIds);
    //         }
    //         if (!candidateUsernames.isEmpty()) {
    //             loginWrapper.or().in(UserLoginLog::getUsername, candidateUsernames);
    //         }
    //         List<UserLoginLog> loginLogs = userLoginLogMapper.selectList(loginWrapper);
    //         allLogs.addAll(loginLogs.stream().map(this::convertToLogResponse).collect(Collectors.toList()));
    //     }

    //     // 查询操作日志
    //     LambdaQueryWrapper<OperationLog> operationWrapper = new LambdaQueryWrapper<>();
    //     buildOperationLogQueryWrapper(operationWrapper, request);
    //     if (!candidateIds.isEmpty()) {
    //         operationWrapper.in(OperationLog::getUserId, candidateIds);
    //     }
    //     if (!candidateUsernames.isEmpty()) {
    //         operationWrapper.or().in(OperationLog::getUsername, candidateUsernames);
    //     }
    //     List<OperationLog> operationLogs = operationLogMapper.selectList(operationWrapper);
    //     allLogs.addAll(operationLogs.stream().map(this::convertToLogResponse).collect(Collectors.toList()));
        
    //     // 按时间排序
    //     allLogs.sort((a, b) -> b.getOperationTime().compareTo(a.getOperationTime()));
        
    //     // 手动分页
    //     int start = (request.getPageNum() - 1) * request.getPageSize();
    //     int end = Math.min(start + request.getPageSize(), allLogs.size());
    //     List<LogResponse> pageData = start < allLogs.size() ? allLogs.subList(start, end) : new ArrayList<>();
        
    //     // 批量补充昵称
    //     Map<String, String> usernameToNickname = loadNicknamesByLogs(
    //         pageData.stream().map(LogResponse::getUsername).filter(Objects::nonNull).distinct().collect(Collectors.toList()),
    //         pageData.stream().map(LogResponse::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList())
    //     );
    //     for (LogResponse r : pageData) {
    //         r.setUserNickname(usernameToNickname.getOrDefault(r.getUsername(), null));
    //     }

    //     IPage<LogResponse> result = new Page<>(request.getPageNum(), request.getPageSize(), allLogs.size());
    //     result.setRecords(pageData);
        
    //     return result;
    // }

    /**
     * 批量加载昵称，构建 username -> nickname 的映射，同时使用userId作为补充
     */
    private Map<String, String> loadNicknamesByLogs(List<String> usernames, List<Long> userIds) {
        Set<Long> idSet = userIds != null ? new HashSet<>(userIds) : new HashSet<>();
        Set<String> unameSet = usernames != null ? new HashSet<>(usernames) : new HashSet<>();
        if (idSet.isEmpty() && unameSet.isEmpty()) {
            return Collections.emptyMap();
        }

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (!idSet.isEmpty()) {
            wrapper.in(User::getId, idSet);
        }
        if (!unameSet.isEmpty()) {
            if (!idSet.isEmpty()) {
                wrapper.or();
            }
            wrapper.in(User::getUsername, unameSet);
        }
        List<User> users = userMapper.selectList(wrapper);
        Map<String, String> map = new HashMap<>();
        for (User u : users) {
            map.put(u.getUsername(), u.getNickname());
        }
        return map;
    }
    
    // 已弃用：统计功能移除后不再需要该构建器
    
    /**
     * 转换操作日志为统一响应格式
     */
    private LogResponse convertToLogResponse(OperationLog operationLog) {
        LogResponse response = new LogResponse();
        response.setId(operationLog.getId());
        response.setLogType("OPERATION");
        response.setUserId(operationLog.getUserId());
        response.setUsername(operationLog.getUsername());
        response.setOperationTime(operationLog.getOperationTime());
        response.setOperationModule(operationLog.getOperationModule());
        response.setOperationType(operationLog.getOperationType());
        response.setOperationDesc(operationLog.getOperationDesc());
        response.setOperationStatus(operationLog.getOperationStatus());
        response.setIpAddress(operationLog.getOperationIp());
        response.setLocation(operationLog.getOperationLocation());
        response.setRequestMethod(operationLog.getRequestMethod());
        response.setRequestUrl(operationLog.getRequestUrl());
        response.setCostTime(operationLog.getCostTime());
        response.setErrorMessage(operationLog.getErrorMessage());
        response.setRequestParams(operationLog.getRequestParams());
        response.setResponseResult(operationLog.getResponseResult());
        return response;
    }
} 
