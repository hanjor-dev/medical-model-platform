/**
 * 积分交易类型枚举：定义积分交易的财务性质
 * 
 * 功能描述：
 * 1. 定义积分交易的财务性质（收入、支出、转账等）
 * 2. 与数据库transaction_type字段对应
 * 3. 与具体业务场景scenarioCode分离
 * 4. 支持交易类型的统一管理
 * 
 * 设计原则：
 * - 只关注交易的财务性质，不涉及具体业务场景
 * - 枚举值与数据库字段值保持一致
 * - 具体业务场景使用CreditScenarioCode管理
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-16 16:30:00
 */
package com.okbug.platform.common.enums.credit;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum CreditTransactionType {
    
    /**
     * 获得积分（所有积分收入场景）
     * 包括：注册奖励、推荐奖励、任务完成、签到奖励等
     */
    EARN("EARN", "收入", TransactionCategory.INCOME),
    
    /**
     * 消费积分（所有积分支出场景）
     * 包括：AI计算、阅片、文件上传、模型下载等
     */
    SPEND("SPEND", "支出", TransactionCategory.EXPENSE),
    
    /**
     * 积分转账（用户间积分转移）
     */
    TRANSFER("TRANSFER", "转账", TransactionCategory.TRANSFER),
    
    /**
     * 积分退款（退回已消费的积分）
     */
    REFUND("REFUND", "退款", TransactionCategory.REFUND),
    
    /**
     * 管理员操作（手动调整积分）
     */
    ADMIN("ADMIN", "管理员操作", TransactionCategory.ADMIN),

    /**
     * 初始化/其他
     */
    REGISTER("REGISTER", "初始化/其他", TransactionCategory.REGISTER);
    
    /**
     * 交易类型编码
     */
    private final String code;
    
    /**
     * 交易类型名称
     */
    private final String name;
    
    /**
     * 交易分类
     */
    private final TransactionCategory category;
    
    CreditTransactionType(String code, String name, TransactionCategory category) {
        this.code = code;
        this.name = name;
        this.category = category;
    }
    
    // ================ 静态方法 ================
    
    /**
     * 根据编码获取枚举
     * 
     * @param code 交易类型编码
     * @return 交易类型枚举，如果不存在返回null
     */
    public static CreditTransactionType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        for (CreditTransactionType type : values()) {
            if (type.code.equals(code.trim())) {
                return type;
            }
        }
        
        return null;
    }
    
    /**
     * 验证编码是否有效
     * 
     * @param code 交易类型编码
     * @return true:有效 false:无效
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }
    
    /**
     * 获取所有交易类型编码列表
     * 
     * @return 所有交易类型编码
     */
    public static List<String> getAllCodes() {
        return Arrays.stream(values())
                .map(CreditTransactionType::getCode)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据交易分类获取交易类型列表
     * 
     * @param category 交易分类
     * @return 指定分类的交易类型列表
     */
    public static List<CreditTransactionType> getByCategory(TransactionCategory category) {
        return Arrays.stream(values())
                .filter(type -> type.category == category)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有收入类型的交易
     * 
     * @return 收入交易类型列表
     */
    public static List<CreditTransactionType> getIncomeTypes() {
        return getByCategory(TransactionCategory.INCOME);
    }
    
    /**
     * 获取所有支出类型的交易
     * 
     * @return 支出交易类型列表
     */
    public static List<CreditTransactionType> getExpenseTypes() {
        return getByCategory(TransactionCategory.EXPENSE);
    }
    
    // ================ 实例方法 ================
    
    /**
     * 判断是否为收入类型
     * 
     * @return true:收入类型 false:非收入类型
     */
    public boolean isIncome() {
        return this.category == TransactionCategory.INCOME;
    }
    
    /**
     * 判断是否为支出类型
     * 
     * @return true:支出类型 false:非支出类型
     */
    public boolean isExpense() {
        return this.category == TransactionCategory.EXPENSE;
    }
    
    /**
     * 判断是否为转账类型
     * 
     * @return true:转账类型 false:非转账类型
     */
    public boolean isTransfer() {
        return this.category == TransactionCategory.TRANSFER;
    }
    
    /**
     * 判断是否为退款类型
     * 
     * @return true:退款类型 false:非退款类型
     */
    public boolean isRefund() {
        return this.category == TransactionCategory.REFUND;
    }
    
    /**
     * 判断是否为管理员操作
     * 
     * @return true:管理员操作 false:非管理员操作
     */
    public boolean isAdminOperation() {
        return this.category == TransactionCategory.ADMIN;
    }
    
    /**
     * 获取交易类型描述
     * 
     * @return 交易类型描述
     */
    public String getDescription() {
        return this.name;
    }
    
    @Override
    public String toString() {
        return this.code;
    }
    
    // ================ 内部枚举 ================
    
    /**
     * 交易分类枚举
     */
    public enum TransactionCategory {
        /**
         * 收入类型（获得积分）
         */
        INCOME,
        
        /**
         * 支出类型（消耗积分）
         */
        EXPENSE,
        
        /**
         * 转账类型（积分转移）
         */
        TRANSFER,
        
        /**
         * 退款类型（退回积分）
         */
        REFUND,
        
        /**
         * 管理员操作（手动调整积分）
         */
        ADMIN,

        /**
         * 初始化/其他
         */
        REGISTER;
    }
}
