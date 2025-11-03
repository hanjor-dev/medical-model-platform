/**
 * 积分余额响应DTO：封装用户积分余额的响应数据
 * 
 * 功能描述：
 * 1. 封装用户积分余额信息
 * 2. 包含积分类型和余额详情
 * 3. 支持多种积分类型
 * 4. 用于前端展示用户积分状态
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 11:25:00
 */
package com.okbug.platform.dto.credit.response;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "积分余额响应")
public class CreditBalanceResponse {
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1234567890")
    private Long userId;
    
    /**
     * 积分账户列表
     */
    @Schema(description = "积分账户列表")
    private List<CreditAccountInfo> accounts;
    
    /**
     * 总积分余额（所有类型）
     */
    @Schema(description = "总积分余额", example = "5000.00")
    private BigDecimal totalBalance;
    
    /**
     * 积分账户数量
     */
    @Schema(description = "积分账户数量", example = "2")
    private Integer accountCount;
    
    /**
     * 查询时间
     */
    @Schema(description = "查询时间", example = "2025-01-15 11:25:00")
    private LocalDateTime queryTime;
    
    /**
     * 积分账户信息
     */
    @Data
    @Schema(description = "积分账户信息")
    public static class CreditAccountInfo {
        
        /**
         * 积分类型编码
         */
        @Schema(description = "积分类型编码", example = "NORMAL")
        private String creditTypeCode;
        
        /**
         * 积分类型名称
         */
        @Schema(description = "积分类型名称", example = "普通积分")
        private String creditTypeName;
        
        /**
         * 积分单位名称
         */
        @Schema(description = "积分单位名称", example = "积分")
        private String unitName;
        
        /**
         * 当前余额
         */
        @Schema(description = "当前余额", example = "3000.00")
        private BigDecimal balance;
        
        /**
         * 累计获得积分
         */
        @Schema(description = "累计获得积分", example = "5000.00")
        private BigDecimal totalEarned;
        
        /**
         * 累计消费积分
         */
        @Schema(description = "累计消费积分", example = "2000.00")
        private BigDecimal totalConsumed;
        
        /**
         * 积分图标URL
         */
        @Schema(description = "积分图标URL", example = "/icons/normal-credit.png")
        private String iconUrl;
        
        /**
         * 显示颜色代码
         */
        @Schema(description = "显示颜色代码", example = "#4A90E2")
        private String colorCode;
        
        /**
         * 是否支持转账
         */
        @Schema(description = "是否支持转账", example = "true")
        private Boolean isTransferable;
        
        /**
         * 小数位数
         */
        @Schema(description = "小数位数", example = "0")
        private Integer decimalPlaces;
        
        /**
         * 最后更新时间
         */
        @Schema(description = "最后更新时间", example = "2025-01-15 11:25:00")
        private LocalDateTime updateTime;
        
        // ================ 便利方法 ================
        
        /**
         * 判断是否有余额
         */
        public boolean hasBalance() {
            return balance != null && balance.compareTo(BigDecimal.ZERO) > 0;
        }
        
        /**
         * 判断余额是否不足
         */
        public boolean hasInsufficientBalance() {
            return balance == null || balance.compareTo(BigDecimal.ZERO) <= 0;
        }
        
        /**
         * 获取可用余额（如果余额为null则返回0）
         */
        public BigDecimal getAvailableBalance() {
            return balance != null ? balance : BigDecimal.ZERO;
        }
        
        /**
         * 获取累计获得积分（如果为null则返回0）
         */
        public BigDecimal getTotalEarnedAmount() {
            return totalEarned != null ? totalEarned : BigDecimal.ZERO;
        }
        
        /**
         * 获取累计消费积分（如果为null则返回0）
         */
        public BigDecimal getTotalConsumedAmount() {
            return totalConsumed != null ? totalConsumed : BigDecimal.ZERO;
        }
        
        /**
         * 判断是否为整数类型
         */
        public boolean isIntegerType() {
            return decimalPlaces != null && decimalPlaces == 0;
        }
        
        /**
         * 判断是否为小数类型
         */
        public boolean isDecimalType() {
            return decimalPlaces != null && decimalPlaces > 0;
        }
        
        /**
         * 获取余额显示文本
         */
        public String getBalanceDisplayText() {
            if (balance == null) {
                return "0";
            }
            
            if (isIntegerType()) {
                return balance.intValue() + "";
            } else {
                return balance.toString();
            }
        }
    }
    
    // ================ 便利方法 ================
    
    /**
     * 获取指定类型的积分账户信息
     */
    public CreditAccountInfo getAccountByType(String creditTypeCode) {
        if (accounts == null || creditTypeCode == null) {
            return null;
        }
        
        for (CreditAccountInfo account : accounts) {
            if (creditTypeCode.equals(account.getCreditTypeCode())) {
                return account;
            }
        }
        
        return null;
    }
    
    /**
     * 获取指定类型的积分余额
     */
    public BigDecimal getBalanceByType(String creditTypeCode) {
        CreditAccountInfo account = getAccountByType(creditTypeCode);
        return account != null ? account.getAvailableBalance() : BigDecimal.ZERO;
    }
    
    /**
     * 判断是否有指定类型的积分账户
     */
    public boolean hasAccountType(String creditTypeCode) {
        return getAccountByType(creditTypeCode) != null;
    }
    
    /**
     * 获取有余额的积分账户数量
     */
    public int getAccountsWithBalanceCount() {
        if (accounts == null) {
            return 0;
        }
        
        int count = 0;
        for (CreditAccountInfo account : accounts) {
            if (account.hasBalance()) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * 获取空余额的积分账户数量
     */
    public int getEmptyAccountsCount() {
        if (accounts == null) {
            return 0;
        }
        
        int count = 0;
        for (CreditAccountInfo account : accounts) {
            if (account.hasInsufficientBalance()) {
                count++;
            }
        }
        
        return count;
    }
} 
