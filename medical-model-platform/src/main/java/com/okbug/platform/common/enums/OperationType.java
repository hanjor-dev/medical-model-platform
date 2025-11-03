package com.okbug.platform.common.enums;

/**
 * 操作类型枚举
 * 建议保持与历史字符串值一致，便于统一口径统计。
 */
public enum OperationType {
    NONE,
    // 通用
    CREATE,
    UPDATE,
    DELETE,
    ENABLE,
    DISABLE,
    EXPORT,
    IMPORT,
    CLEAN,
    BATCH_DELETE,
    BATCH_UPDATE,
    SORT_UPDATE,
    ALLOCATE,

    // 细分类型（与现有字符串对齐）
    TYPE_CREATE,
    TYPE_UPDATE,
    TYPE_DELETE,
    TYPE_ENABLE,
    TYPE_DISABLE,
    TYPE_SORT_UPDATE,
    SCENARIO_CREATE,
    SCENARIO_UPDATE,
    SCENARIO_DELETE,
    SCENARIO_ENABLE,
    SCENARIO_DISABLE,
    SCENARIO_EXECUTE
}


