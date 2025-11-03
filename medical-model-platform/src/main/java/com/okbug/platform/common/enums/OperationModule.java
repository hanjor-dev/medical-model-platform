package com.okbug.platform.common.enums;

/**
 * 操作模块枚举
 * 使用英文标识作为真实值，并提供中文显示名称。
 */
public enum OperationModule {
    NONE("通用"),
    AUTH("认证"),
    USER("用户"),
    PERMISSION("权限"),
    SYSTEM("系统"),
    CONFIG("配置"),
    LOG("日志"),
    CREDIT("积分"),
    MODEL("模型"),
    TASK("任务"),
    FILE("文件");

    private final String label;

    OperationModule(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}


