/**
 * 操作日志切面
 * 
 * 通过AOP自动记录用户操作日志
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:35:00
 */
package com.okbug.platform.common.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.utils.IpUtils;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;
import com.okbug.platform.service.log.UserLogService;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.entity.auth.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class OperationLogAspect {
    
    private final UserLogService userLogService;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    
    @Around("@annotation(com.okbug.platform.common.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;
        Long preUserId = null;
        String preUsername = null;
        HttpServletRequest request = null;
        Object[] args = joinPoint.getArgs();
        
        // 提前获取请求与用户信息，避免如登出后无法获取用户的问题
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                request = attributes.getRequest();
            }
            long[] userInfo = getUserInfoBeforeProceed(request);
            if (userInfo != null) {
                preUserId = userInfo[0] != -1 ? userInfo[0] : null;
            }
            preUsername = tryResolveUsername(preUserId);
        } catch (Exception ignore) {
        }
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            // 记录操作日志
            recordOperationLog(joinPoint, request, args, preUserId, preUsername, result, exception, startTime);
        }
    }
    
    /**
     * 记录操作日志
     */
    private void recordOperationLog(ProceedingJoinPoint joinPoint, HttpServletRequest request, Object[] argsSnapshot,
                                    Long preUserId, String preUsername, Object result, Exception exception, long startTime) {
        try {
            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperationLog operationLogAnnotation = method.getAnnotation(OperationLog.class);
            
            if (operationLogAnnotation == null) {
                return;
            }
            
            // 请求对象兜底
            if (request == null) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    request = attributes.getRequest();
                }
            }
            
            // 获取用户信息（优先使用前置捕获）
            Long userId = preUserId;
            String username = preUsername;
            if (userId == null) {
                try {
                    if (StpUtil.isLogin()) {
                        userId = StpUtil.getLoginIdAsLong();
                        if (username == null) {
                            username = (String) StpUtil.getSession().get("username");
                        }
                    } else if (request != null) {
                        // 从请求头中获取 Token 尝试解析用户
                        String tokenValue = extractTokenFromRequest(request);
                        if (tokenValue != null) {
                            try {
                                Object idObj = cn.dev33.satoken.stp.StpUtil.getLoginIdByToken(tokenValue);
                                if (idObj != null) {
                                    if (idObj instanceof Number) {
                                        userId = ((Number) idObj).longValue();
                                    } else {
                                        userId = Long.valueOf(String.valueOf(idObj));
                                    }
                                }
                            } catch (Exception ignore) {
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("获取用户信息失败，记录为匿名操作", e);
                }
                // 通过用户ID回填用户名
                if (username == null && userId != null) {
                    username = tryResolveUsername(userId);
                }
            }
            
            // 构建操作日志
            com.okbug.platform.entity.auth.OperationLog operationLog = new com.okbug.platform.entity.auth.OperationLog();
            operationLog.setUserId(userId);
            operationLog.setUsername(username);
            String moduleStr = resolveModule(operationLogAnnotation);
            String typeStr = resolveType(operationLogAnnotation);
            operationLog.setOperationModule(moduleStr);
            operationLog.setOperationType(typeStr);
            operationLog.setOperationDesc(operationLogAnnotation.description());
            if (request != null) {
                operationLog.setRequestMethod(request.getMethod());
                operationLog.setRequestUrl(request.getRequestURI());
                operationLog.setOperationIp(IpUtils.getClientIp(request));
                operationLog.setOperationLocation(IpUtils.getLocationByIp(operationLog.getOperationIp()));
            }
            operationLog.setOperationTime(LocalDateTime.now());
            operationLog.setCostTime(System.currentTimeMillis() - startTime);
            
            // 记录请求参数
            if (operationLogAnnotation.recordParams()) {
                String params = buildRequestParamsString(argsSnapshot, request);
                if (params != null && !params.isEmpty()) {
                    operationLog.setRequestParams(params);
                }
            }
            
            // 记录响应结果
            if (operationLogAnnotation.recordResult() && result != null) {
                try {
                    Object safeResult = maskSensitive(result);
                    String resultStr = objectMapper.writeValueAsString(safeResult);
                    // 限制结果长度
                    if (resultStr.length() > 2000) {
                        resultStr = resultStr.substring(0, 2000) + "...";
                    }
                    operationLog.setResponseResult(resultStr);
                } catch (Exception e) {
                    operationLog.setResponseResult("结果序列化失败: " + e.getMessage());
                }
            }
            
            // 设置操作状态和错误信息
            if (exception != null) {
                operationLog.setOperationStatus(com.okbug.platform.entity.auth.OperationLog.STATUS_FAILED);
                operationLog.setErrorMessage(exception.getMessage());
            } else {
                operationLog.setOperationStatus(com.okbug.platform.entity.auth.OperationLog.STATUS_SUCCESS);
            }
            
            // 记录日志（异步或同步）
            if (operationLogAnnotation.async()) {
                userLogService.recordOperationLogAsync(operationLog);
            } else {
                userLogService.recordOperationLog(operationLog);
            }
            
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
            // 日志记录失败不应影响主业务
        }
    }

    private String resolveModule(OperationLog annotation) {
        try {
            OperationModule moduleEnum = annotation.moduleEnum();
            if (moduleEnum != null && moduleEnum != OperationModule.NONE) {
                return moduleEnum.name();
            }
        } catch (Throwable ignore) {
        }
        String module = annotation.module();
        return module != null ? module : null;
    }

    private String resolveType(OperationLog annotation) {
        try {
            OperationType typeEnum = annotation.typeEnum();
            if (typeEnum != null && typeEnum != OperationType.NONE) {
                return typeEnum.name();
            }
        } catch (Throwable ignore) {
        }
        String type = annotation.type();
        return type != null ? type : null;
    }

    private long[] getUserInfoBeforeProceed(HttpServletRequest request) {
        try {
            if (StpUtil.isLogin()) {
                long uid = StpUtil.getLoginIdAsLong();
                return new long[]{uid};
            }
            // 从请求头尝试解析
            if (request != null) {
                String tokenValue = extractTokenFromRequest(request);
                if (tokenValue != null) {
                    Object idObj = StpUtil.getLoginIdByToken(tokenValue);
                    if (idObj != null) {
                        if (idObj instanceof Number) {
                            return new long[]{((Number) idObj).longValue()};
                        }
                        return new long[]{Long.valueOf(String.valueOf(idObj))};
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    private String tryResolveUsername(Long userId) {
        if (userId == null) return null;
        try {
            User user = userMapper.selectById(userId);
            return user != null ? user.getUsername() : null;
        } catch (Exception ignore) {
            return null;
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        if (request == null) return null;
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            return auth.substring(7).trim();
        }
        String saToken = request.getHeader("satoken");
        if (saToken != null && !saToken.isEmpty()) {
            return saToken;
        }
        return null;
    }

    private String buildRequestParamsString(Object[] args, HttpServletRequest request) {
        Map<String, Object> collected = new LinkedHashMap<>();
        // Query/Form params
        if (request != null) {
            Map<String, String[]> paramMap = request.getParameterMap();
            if (paramMap != null && !paramMap.isEmpty()) {
                Map<String, Object> simple = new LinkedHashMap<>();
                for (Map.Entry<String, String[]> e : paramMap.entrySet()) {
                    String key = e.getKey();
                    Object value = e.getValue();
                    if (value instanceof String[]) {
                        simple.put(key, String.join(",", (String[]) value));
                    } else {
                        simple.put(key, value);
                    }
                }
                collected.put("query", maskSensitive(simple));
            }
        }
        // Body args
        if (args != null && args.length > 0) {
            List<Object> bodyArgs = new ArrayList<>();
            for (Object arg : args) {
                if (arg == null) continue;
                if (isSkippableArg(arg)) continue;
                bodyArgs.add(arg);
            }
            if (!bodyArgs.isEmpty()) {
                collected.put("body", maskSensitive(bodyArgs));
            }
        }
        if (collected.isEmpty()) return null;
        try {
            String json = objectMapper.writeValueAsString(collected);
            if (json.length() > 2000) {
                json = json.substring(0, 2000) + "...";
            }
            return json;
        } catch (Exception e) {
            return "参数序列化失败: " + e.getMessage();
        }
    }

    private boolean isSkippableArg(Object arg) {
        return (arg instanceof HttpServletRequest)
            || (arg instanceof HttpServletResponse)
            || (arg instanceof ServletRequest)
            || (arg instanceof ServletResponse)
            || (arg instanceof BindingResult)
            || (arg instanceof MultipartFile)
            || (arg instanceof MultipartFile[]);
    }

    private Object maskSensitive(Object data) {
        try {
            // 将对象转换为通用结构便于脱敏
            String json = objectMapper.writeValueAsString(data);
            // 简单脱敏：password/oldPassword/newPassword/secret/token
            json = json.replaceAll("\\\"password\\\"\\s*:\\s*\\\".*?\\\"", "\"password\":\"***\"");
            json = json.replaceAll("\\\"oldPassword\\\"\\s*:\\s*\\\".*?\\\"", "\"oldPassword\":\"***\"");
            json = json.replaceAll("\\\"newPassword\\\"\\s*:\\s*\\\".*?\\\"", "\"newPassword\":\"***\"");
            json = json.replaceAll("\\\"secret\\\"\\s*:\\s*\\\".*?\\\"", "\"secret\":\"***\"");
            json = json.replaceAll("\\\"token\\\"\\s*:\\s*\\\".*?\\\"", "\"token\":\"***\"");
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return data;
        }
    }
} 
