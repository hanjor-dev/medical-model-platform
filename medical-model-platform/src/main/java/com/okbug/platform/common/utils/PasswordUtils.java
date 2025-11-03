/**
 * 密码工具类：提供密码加密、验证和强度检查功能
 * 
 * 核心功能：
 * 1. 密码BCrypt加密和验证（使用Hutool实现）
 * 2. 密码强度验证（8-20位，包含数字和字母）
 * 3. 密码安全性检查
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 02:35:00
 */
package com.okbug.platform.common.utils;

import cn.hutool.crypto.digest.BCrypt;
import java.util.regex.Pattern;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.common.base.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PasswordUtils {
    
    /**
     * 密码强度正则表达式：8-20位，至少包含一个数字和一个字母
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z]).{8,20}$");
    
    /**
     * 弱密码列表（常见的弱密码）
     */
    private static final String[] WEAK_PASSWORDS = {
        "12345678", "password", "123456789", "qwertyui", "12345678", 
        "admin123", "password123", "qwerty123", "abc123456", "123123123"
    };
    
    /**
     * 加密密码
     * 使用Hutool的BCrypt实现
     * 
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    public static String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "密码不能为空");
        }
        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        log.debug("Password encoded using BCrypt with length: {}", hashed.length());
        return hashed;
    }
    
    /**
     * 验证密码
     * 使用Hutool的BCrypt实现
     * 
     * @param rawPassword 明文密码
     * @param encodedPassword 加密密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        try {
            boolean result = BCrypt.checkpw(rawPassword, encodedPassword);
            log.debug("Password match check completed: {}", result);
            return result;
        } catch (Exception e) {
            // 避免泄露敏感信息，仅记录异常类型
            log.warn("BCrypt password match threw exception: {}", e.getClass().getSimpleName());
            return false;
        }
    }
    
    /**
     * 验证密码强度
     * 
     * @param password 待验证的密码
     * @return 是否符合强度要求
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        // 基本格式验证：8-20位，包含数字和字母
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return false;
        }
        
        // 检查是否为常见弱密码
        String lowerPassword = password.toLowerCase();
        for (String weakPassword : WEAK_PASSWORDS) {
            if (lowerPassword.equals(weakPassword)) {
                return false;
            }
        }
        
        // 检查是否有连续相同字符（超过3个）
        if (hasRepeatingChars(password, 3)) {
            return false;
        }
        
        // 检查是否为纯数字或纯字母
        if (password.matches("^[0-9]+$") || password.matches("^[a-zA-Z]+$")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查密码是否包含连续重复字符
     * 
     * @param password 密码
     * @param maxRepeat 最大重复次数
     * @return 是否包含超过指定次数的重复字符
     */
    private static boolean hasRepeatingChars(String password, int maxRepeat) {
        if (password.length() < maxRepeat) {
            return false;
        }
        
        int count = 1;
        char prevChar = password.charAt(0);
        
        for (int i = 1; i < password.length(); i++) {
            char currentChar = password.charAt(i);
            if (currentChar == prevChar) {
                count++;
                if (count > maxRepeat) {
                    return true;
                }
            } else {
                count = 1;
                prevChar = currentChar;
            }
        }
        
        return false;
    }
    
    /**
     * 获取密码强度描述
     * 
     * @param password 密码
     * @return 密码强度描述
     */
    public static String getPasswordStrengthDescription(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "密码不能为空";
        }
        
        if (password.length() < 8) {
            return "密码长度不能少于8位";
        }
        
        if (password.length() > 20) {
            return "密码长度不能超过20位";
        }
        
        if (!password.matches(".*[0-9].*")) {
            return "密码必须包含至少一个数字";
        }
        
        if (!password.matches(".*[a-zA-Z].*")) {
            return "密码必须包含至少一个字母";
        }
        
        String lowerPassword = password.toLowerCase();
        for (String weakPassword : WEAK_PASSWORDS) {
            if (lowerPassword.equals(weakPassword)) {
                return "密码过于简单，请使用更复杂的密码";
            }
        }
        
        if (hasRepeatingChars(password, 3)) {
            return "密码不能包含超过3个连续相同字符";
        }
        
        if (password.matches("^[0-9]+$")) {
            return "密码不能为纯数字";
        }
        
        if (password.matches("^[a-zA-Z]+$")) {
            return "密码不能为纯字母";
        }
        
        return "密码强度符合要求";
    }
    
    /**
     * 生成安全的随机盐值
     * 
     * @return BCrypt盐值
     */
    public static String generateSalt() {
        String salt = BCrypt.gensalt();
        log.debug("BCrypt salt generated with length: {}", salt.length());
        return salt;
    }
    
    /**
     * 使用指定盐值加密密码
     * 
     * @param rawPassword 明文密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    public static String encodePasswordWithSalt(String rawPassword, String salt) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "密码不能为空");
        }
        if (salt == null || salt.trim().isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "盐值不能为空");
        }
        String hashed = BCrypt.hashpw(rawPassword, salt);
        log.debug("Password encoded with provided salt. Hash length: {}", hashed.length());
        return hashed;
    }
} 