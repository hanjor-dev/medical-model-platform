/**
 * 推荐码工具类：提供推荐码生成和验证功能
 * 
 * 核心功能：
 * 1. 生成8位唯一推荐码
 * 2. 推荐码格式验证
 * 3. 推荐码唯一性检查
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-14 23:40:00
 */
package com.okbug.platform.common.utils;

import java.security.SecureRandom;
import java.util.regex.Pattern;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.common.base.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReferralCodeUtils {
    
    /**
     * 推荐码字符集：数字+大小写字母（排除容易混淆的字符：0,O,1,I,l）
     */
    private static final String CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
    
    /**
     * 推荐码长度
     */
    private static final int CODE_LENGTH = 8;
    
    /**
     * 推荐码格式验证正则表达式
     */
    private static final Pattern CODE_PATTERN = Pattern.compile("^[" + CHARACTERS + "]{" + CODE_LENGTH + "}$");
    
    /**
     * 安全随机数生成器
     */
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * 生成推荐码
     * 
     * 说明：使用安全随机数从字符集生成固定长度的推荐码
     * 
     * @return 8位推荐码
     */
    public static String generateReferralCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        
        String code = sb.toString();
        log.debug("Referral code generated with length: {}", code.length());
        return code;
    }
    
    /**
     * 验证推荐码格式
     * 
     * 规则：长度固定为8，字符来自预定义字符集，排除易混字符
     * 
     * @param referralCode 推荐码
     * @return 格式是否正确
     */
    public static boolean isValidFormat(String referralCode) {
        if (referralCode == null || referralCode.trim().isEmpty()) {
            return false;
        }
        return CODE_PATTERN.matcher(referralCode.trim()).matches();
    }
    
    /**
     * 清理推荐码（去除空格，转换大小写等）
     * 
     * 说明：当前仅去除前后空白，不改变字符大小写
     * 
     * @param referralCode 原始推荐码
     * @return 清理后的推荐码；若为空或清理后为空返回null
     */
    public static String cleanReferralCode(String referralCode) {
        if (referralCode == null) {
            return null;
        }
        
        // 去除空格并转换为标准格式
        String cleaned = referralCode.trim();
        
        // 如果为空，返回null
        if (cleaned.isEmpty()) {
            return null;
        }
        
        return cleaned;
    }
    
    /**
     * 生成唯一推荐码（需要外部提供唯一性检查函数）
     * 
     * @param uniquenessChecker 唯一性检查函数，返回true表示推荐码已存在
     * @param maxAttempts 最大尝试次数
     * @return 唯一的推荐码
     * @throws ServiceException 超过最大尝试次数仍无法生成唯一推荐码
     */
    public static String generateUniqueReferralCode(java.util.function.Function<String, Boolean> uniquenessChecker, int maxAttempts) {
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            String code = generateReferralCode();
            
            // 如果推荐码不存在（uniquenessChecker返回false），则表示找到了唯一的推荐码
            if (!uniquenessChecker.apply(code)) {
                log.debug("Unique referral code acquired on attempt {}", attempt + 1);
                return code;
            }
            log.debug("Referral code collision on attempt {}", attempt + 1);
        }
        
        log.warn("Failed to generate a unique referral code after {} attempts", maxAttempts);
        throw new ServiceException(ErrorCode.REFERRAL_CODE_GENERATION_FAILED, "无法生成唯一推荐码，已尝试 " + maxAttempts + " 次");
    }
    
    /**
     * 生成唯一推荐码（默认最大尝试100次）
     * 
     * @param uniquenessChecker 唯一性检查函数
     * @return 唯一的推荐码
     */
    public static String generateUniqueReferralCode(java.util.function.Function<String, Boolean> uniquenessChecker) {
        return generateUniqueReferralCode(uniquenessChecker, 100);
    }

    public static void main(String[] args) {
        String code = "SUPER888";
        System.out.println(isValidFormat(code));
    }
} 