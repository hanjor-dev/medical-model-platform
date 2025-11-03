/**
 * 错误码枚举：定义系统中所有的错误码
 * 
 * 设计原则：
 * 1. 分类清晰 - 按模块划分错误码范围
 * 2. 易于扩展 - 预留足够的错误码空间
 * 3. 语义明确 - 错误码名称和消息描述准确
 * 
 * 错误码规则：
 * - 200: 成功
 * - 400-499: 客户端错误
 * - 500-599: 服务端错误
 * - 1000+: 业务错误码
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-14 22:15:00
 */
package com.okbug.platform.common.base;

public enum ErrorCode {
    
    // ================ 基础状态码 ================
    SUCCESS(200, "操作成功"),
    
    // ================ 客户端错误 4xx ================
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "访问被拒绝"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    REQUEST_TIMEOUT(408, "请求超时"),
    CONFLICT(409, "资源冲突"),
    PAYLOAD_TOO_LARGE(413, "请求体过大"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的媒体类型"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    
    // ================ 服务端错误 5xx ================
    INTERNAL_ERROR(500, "服务器内部错误"),
    NOT_IMPLEMENTED(501, "功能未实现"),
    BAD_GATEWAY(502, "网关错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),
    
    // ================ 参数验证错误 1000-1099 ================
    PARAM_MISSING(1001, "缺少必要参数"),
    PARAM_INVALID(1002, "参数格式错误"),
    PARAM_OUT_OF_RANGE(1003, "参数超出允许范围"),
    PARAM_TYPE_MISMATCH(1004, "参数类型不匹配"),
    
    // ================ 文件操作错误 1100-1199 ================
    FILE_NOT_FOUND(1101, "文件不存在"),
    FILE_UPLOAD_FAILED(1102, "文件上传失败"),
    FILE_DELETE_FAILED(1103, "文件删除失败"),
    FILE_SIZE_EXCEEDED(1104, "文件大小超出限制"),
    FILE_TYPE_NOT_SUPPORTED(1105, "文件类型不支持"),
    FILE_DOWNLOAD_FAILED(1106, "文件下载失败"),
    
    // ================ 数据库操作错误 1200-1299 ================
    DATA_NOT_FOUND(1201, "数据不存在"),
    DATA_ALREADY_EXISTS(1202, "数据已存在"),
    DATA_CONSTRAINT_VIOLATION(1203, "数据约束冲突"),
    DATABASE_CONNECTION_FAILED(1204, "数据库连接失败"),
    TRANSACTION_FAILED(1205, "事务执行失败"),
    
    // ================ 业务逻辑错误 1300-1399 ================
    BUSINESS_RULE_VIOLATION(1301, "违反业务规则"),
    OPERATION_NOT_ALLOWED(1302, "操作不被允许"),
    RESOURCE_LOCKED(1303, "资源被锁定"),
    OPERATION_TIMEOUT(1304, "操作超时"),
    
    // ================ 任务相关错误 1400-1499 ================
    TASK_NOT_FOUND(1401, "任务不存在"),
    TASK_ALREADY_RUNNING(1402, "任务正在运行"),
    TASK_EXECUTION_FAILED(1403, "任务执行失败"),
    TASK_CANCELLED(1404, "任务已取消"),
    
    // ================ 模型相关错误 1500-1599 ================
    MODEL_NOT_FOUND(1501, "模型不存在"),
    MODEL_LOADING_FAILED(1502, "模型加载失败"),
    MODEL_INFERENCE_FAILED(1503, "模型推理失败"),
    MODEL_FORMAT_ERROR(1504, "模型格式错误"),
    
    // ================ 外部服务错误 1600-1699 ================
    EXTERNAL_SERVICE_ERROR(1601, "外部服务错误"),
    EXTERNAL_SERVICE_TIMEOUT(1602, "外部服务超时"),
    EXTERNAL_SERVICE_UNAVAILABLE(1603, "外部服务不可用"),
    
    // ================ 用户认证相关错误 1700-1799 ================
    USER_NOT_FOUND(1701, "用户不存在"),
    USER_PASSWORD_WRONG(1702, "密码错误"),
    USER_ACCOUNT_LOCKED(1703, "账户已锁定"),
    USER_ACCOUNT_DISABLED(1704, "账户已禁用"),
    USER_TOKEN_EXPIRED(1705, "登录令牌已过期"),
    USER_TOKEN_INVALID(1706, "登录令牌无效"),
    USERNAME_ALREADY_EXISTS(1707, "用户名已存在"),
    EMAIL_ALREADY_EXISTS(1708, "邮箱已存在"),
    REFERRAL_CODE_INVALID(1709, "推荐码无效"),
    LOGIN_FAILED_TOO_MANY(1710, "登录失败次数过多"),
    OLD_PASSWORD_WRONG(1711, "原密码错误"),
    PASSWORD_TOO_WEAK(1712, "密码强度不足"),
    USER_REGISTER_DISABLED(1713, "用户注册功能未开启"),
    USER_REGISTER_REWARD_DISABLED(1714, "用户注册奖励功能未开启"),
    USER_REFERRAL_REWARD_DISABLED(1715, "引荐奖励功能未开启"),
    
    // ================ 引荐码相关错误 1720-1729 ================
    REFERRAL_CODE_NOT_FOUND(1720, "引荐码不存在"),
    REFERRAL_CODE_ALREADY_EXISTS(1721, "引荐码已存在"),
    REFERRAL_CODE_GENERATION_FAILED(1722, "引荐码生成失败"),
    REFERRAL_CODE_FORMAT_INVALID(1723, "引荐码格式无效"),
    REFERRAL_BASE_URL_NOT_CONFIGURED(1724, "引荐码基础链接未配置"),
    REFERRAL_LINK_GENERATION_FAILED(1725, "引荐链接生成失败"),
    REFERRAL_USER_NOT_FOUND(1726, "引荐用户不存在"),
    REFERRAL_USER_DISABLED(1727, "引荐用户账户已禁用"),
    REFERRAL_RELATIONSHIP_INVALID(1728, "引荐关系无效"),
    REFERRAL_QUERY_FAILED(1729, "引荐信息查询失败"),
    
    // ================ 权限管理相关错误 1800-1899 ================
    PERMISSION_DENIED(1801, "权限不足"),
    ROLE_NOT_FOUND(1802, "角色不存在"),
    ROLE_ALREADY_EXISTS(1803, "角色已存在"),
    PERMISSION_NOT_FOUND(1804, "权限不存在"),
    PERMISSION_ALREADY_EXISTS(1805, "权限已存在"),
    PERMISSION_UPDATED(1806, "权限已更新"),
    
    // ================ 积分系统相关错误 1900-1999 ================
    CREDIT_TYPE_NOT_FOUND(1901, "积分类型不存在"),
    CREDIT_TYPE_ALREADY_EXISTS(1902, "积分类型已存在"),
    CREDIT_TYPE_NAME_ALREADY_EXISTS(1903, "积分类型名称已存在"),
    CREDIT_TYPE_CODE_INVALID(1904, "积分类型编码无效"),
    CREDIT_TYPE_DISABLED(1905, "积分类型已禁用"),
    CREDIT_SCENARIO_NOT_FOUND(1906, "积分使用场景不存在"),
    CREDIT_SCENARIO_ALREADY_EXISTS(1907, "积分使用场景已存在"),
    CREDIT_SCENARIO_CODE_INVALID(1908, "积分使用场景编码无效"),
    CREDIT_SCENARIO_DISABLED(1909, "积分使用场景已禁用"),
    USER_CREDIT_ACCOUNT_NOT_FOUND(1910, "用户积分账户不存在"),
    USER_CREDIT_ACCOUNT_ALREADY_EXISTS(1911, "用户积分账户已存在"),
    INSUFFICIENT_CREDIT_BALANCE(1912, "积分余额不足"),
    CREDIT_TRANSACTION_FAILED(1913, "积分交易失败"),
    CREDIT_TRANSACTION_NOT_FOUND(1913 + 100, "积分交易记录不存在"),
    CREDIT_TRANSFER_NOT_ALLOWED(1914, "积分转账不被允许"),
    CREDIT_TRANSFER_INSUFFICIENT_BALANCE(1915, "转账积分余额不足"),
    CREDIT_TRANSFER_SAME_USER(1916, "不能向自己转账"),
    CREDIT_TRANSFER_AMOUNT_INVALID(1917, "转账金额无效"),
    CREDIT_CONSUMPTION_RULE_NOT_FOUND(1918, "积分消费规则不存在"),
    CREDIT_REWARD_RULE_NOT_FOUND(1919, "积分奖励规则不存在"),
    CREDIT_DAILY_LIMIT_EXCEEDED(1920, "积分使用次数超过每日限制"),
    CREDIT_USER_ROLE_NOT_ALLOWED(1921, "用户角色不允许使用此积分场景"),
    CREDIT_SYSTEM_BESSY(1922, "积分系统繁忙，请稍后再试~"),
    CREDIT_TYPE_DELETE_FORBIDDEN_HAS_BALANCE(1923, "存在非零余额账户，禁止删除该积分类型"),

    // ================ 积分兑换码相关错误 1930-1949 ================
    REDEEM_CODE_INVALID(1930, "兑换码无效"),
    REDEEM_CODE_EXPIRED(1931, "兑换码已过期"),
    REDEEM_CODE_USED(1932, "兑换码已被使用"),
    REDEEM_CODE_TYPE_MISMATCH(1933, "兑换码与积分类型不匹配"),
    REDEEM_CODE_GENERATION_FAILED(1934, "兑换码生成失败"),
    REDEEM_SECRET_NOT_CONFIGURED(1935, "兑换码加密密钥未配置"),
    REDEEM_TRY_TOO_MANY(1936, "兑换失败次数过多，请稍后再试"),
    REDEEM_USER_TEMP_LOCKED(1937, "账户已临时锁定，请稍后再试"),

    // ================ 日志管理相关错误 2300-2399 ================
    LOG_NOT_FOUND(2301, "日志不存在"),
    LOG_TYPE_INVALID(2302, "日志类型无效"),
    LOG_EXPORT_FAILED(2303, "日志导出失败"),
    LOG_CLEANUP_FAILED(2304, "日志清理失败"),
    LOG_QUERY_FAILED(2305, "日志查询失败"),
    
    // ================ 系统配置相关错误 2400-2499 ================
    CONFIG_CATEGORY_CODE_INVALID(2401, "配置分类编码无效"),
    CONFIG_TYPE_CODE_INVALID(2402, "配置类型编码无效"),
    CONFIG_CATEGORY_MISMATCH(2403, "配置分类编码与名称不一致"),
    CONFIG_TYPE_MISMATCH(2404, "配置类型编码与名称不一致"),
    
    // ===== 消息与公告（系统/消息） 2450-2469 =====
    NOTIFY_TEMPLATE_NOT_FOUND(2451, "消息模板不存在"),
    NOTIFY_TEMPLATE_DISABLED(2452, "消息模板已禁用"),
    NOTIFY_CHANNEL_DISABLED(2453, "通知通道未启用"),
    NOTIFY_SCHEDULE_INVALID(2454, "消息定时配置无效"),
    NOTIFY_PUSH_EXECUTE_FAILED(2455, "消息推送执行失败"),
    NOTIFY_TEMPLATE_INVALID(2456, "消息模板变量校验失败"),
    NOTIFY_MESSAGE_NOT_FOUND(2457, "消息不存在"),
    NOTIFY_ANNOUNCEMENT_NOT_FOUND(2458, "公告不存在"),
    NOTIFY_PREF_VALIDATE_FAILED(2459, "通知偏好校验失败"),
    NOTIFY_PREF_NOT_FOUND(2460, "通知偏好不存在"),
    NOTIFY_EVENT_UNSUPPORTED(2461, "不支持的通知事件"),

    // ================ 团队相关错误 2600-2699 ================
    TEAM_NOT_FOUND(2601, "团队不存在"),
    TEAM_CODE_INVALID(2602, "团队码无效"),
    TEAM_DISABLED(2603, "团队已禁用"),
    TEAM_MEMBER_ALREADY_EXISTS(2604, "已是该团队成员"),
    TEAM_JOIN_REQUEST_ALREADY_EXISTS(2605, "已提交加入申请，待处理"),
    JOIN_REQUEST_NOT_FOUND(2606, "加入申请不存在"),
    JOIN_REQUEST_ALREADY_PROCESSED(2607, "加入申请已处理"),
    TEAM_MEMBER_NOT_FOUND(2608, "团队成员不存在"),
    TEAM_MEMBER_ROLE_INVALID(2609, "团队成员角色无效"),
    INVITATION_NOT_FOUND(2610, "邀请不存在"),
    INVITATION_TOKEN_INVALID(2611, "邀请令牌无效"),
    INVITATION_EXPIRED(2612, "邀请已过期"),
    INVITATION_ALREADY_ACCEPTED(2613, "邀请已被接受"),
    INVITATION_GENERATION_FAILED(2614, "邀请生成失败"),
    // 限流与幂等（团队）
    TEAM_RATE_LIMITED(2620, "操作过于频繁，请稍后重试"),
    TEAM_IDEMPOTENT_REPLAY(2621, "请求已处理，请勿重复提交");
    
    private final Integer code;
    private final String message;
    
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    /**
     * 根据错误码查找枚举
     */
    public static ErrorCode getByCode(Integer code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "ErrorCode{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
} 