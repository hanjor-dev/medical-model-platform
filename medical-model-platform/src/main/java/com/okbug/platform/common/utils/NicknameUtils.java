/**
 * 昵称工具类：提供昵称生成、验证和唯一性检查功能
 * 
 * 功能描述：
 * 1. 自动生成随机昵称（用户+随机数格式）
 * 2. 验证昵称格式和长度
 * 3. 检查昵称唯一性
 * 4. 支持昵称重试生成机制
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 02:30:00
 */
package com.okbug.platform.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.function.Predicate;

@Slf4j
public class NicknameUtils {
    
    /**
     * 昵称最小长度
     */
    private static final int MIN_NICKNAME_LENGTH = 2;
    
    /**
     * 昵称最大长度
     */
    private static final int MAX_NICKNAME_LENGTH = 20;
    
    /**
     * 随机昵称前缀
     */
    private static final String NICKNAME_PREFIX = "用户";
    
    /**
     * 随机数最小值（4位数字）
     */
    private static final int MIN_RANDOM_NUMBER = 1000;
    
    /**
     * 随机数最大值（4位数字）
     */
    private static final int MAX_RANDOM_NUMBER = 9999;
    
    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 10;
    
    /**
     * 随机数生成器
     */
    private static final Random RANDOM = new Random();
    
    /**
     * 生成随机昵称
     * 
     * @return 生成的随机昵称（格式：用户+4位随机数）
     */
    public static String generateRandomNickname() {
        int randomNumber = MIN_RANDOM_NUMBER + RANDOM.nextInt(MAX_RANDOM_NUMBER - MIN_RANDOM_NUMBER + 1);
        return NICKNAME_PREFIX + randomNumber;
    }
    
    /**
     * 生成唯一昵称
     * 
     * @param uniquenessChecker 唯一性检查函数，返回true表示昵称已存在
     * @return 生成的唯一昵称
     */
    public static String generateUniqueNickname(Predicate<String> uniquenessChecker) {
        if (uniquenessChecker == null) {
            log.warn("唯一性检查函数为空，使用随机昵称");
            return generateRandomNickname();
        }
        
        for (int i = 0; i < MAX_RETRY_COUNT; i++) {
            String nickname = generateRandomNickname();
            
            // 检查昵称是否已存在
            if (!uniquenessChecker.test(nickname)) {
                log.debug("生成唯一昵称成功: {}, 重试次数: {}", nickname, i);
                return nickname;
            }
            
            log.debug("昵称已存在，重新生成: {}, 重试次数: {}", nickname, i + 1);
        }
        
        // 达到最大重试次数，生成带时间戳的昵称
        String timestampNickname = NICKNAME_PREFIX + System.currentTimeMillis() % 10000;
        log.warn("达到最大重试次数，使用时间戳昵称: {}", timestampNickname);
        return timestampNickname;
    }
    
    /**
     * 验证昵称格式
     * 
     * @param nickname 待验证的昵称
     * @return true表示昵称格式有效，false表示无效
     */
    public static boolean isValidNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return false;
        }
        
        String trimmedNickname = nickname.trim();
        
        // 检查长度
        if (trimmedNickname.length() < MIN_NICKNAME_LENGTH || trimmedNickname.length() > MAX_NICKNAME_LENGTH) {
            return false;
        }
        
        // 检查是否只包含空白字符
        if (trimmedNickname.trim().isEmpty()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 清理昵称
     * 
     * @param nickname 原始昵称
     * @return 清理后的昵称，如果无效则返回null
     */
    public static String cleanNickname(String nickname) {
        if (nickname == null) {
            return null;
        }
        
        String cleaned = nickname.trim();
        
        if (cleaned.isEmpty()) {
            return null;
        }
        
        // 如果清理后的昵称长度超出限制，截取到最大长度
        if (cleaned.length() > MAX_NICKNAME_LENGTH) {
            cleaned = cleaned.substring(0, MAX_NICKNAME_LENGTH);
            log.debug("昵称长度超出限制，已截取到最大长度: {}", cleaned);
        }
        
        return cleaned;
    }
    
    /**
     * 获取昵称长度限制信息
     * 
     * @return 昵称长度限制描述
     */
    public static String getLengthDescription() {
        return String.format("昵称长度必须在%d-%d个字符之间", MIN_NICKNAME_LENGTH, MAX_NICKNAME_LENGTH);
    }
    
    /**
     * 检查昵称是否为系统生成的随机昵称
     * 
     * @param nickname 待检查的昵称
     * @return true表示是系统生成的随机昵称
     */
    public static boolean isSystemGeneratedNickname(String nickname) {
        if (nickname == null || !nickname.startsWith(NICKNAME_PREFIX)) {
            return false;
        }
        
        try {
            String numberPart = nickname.substring(NICKNAME_PREFIX.length());
            int number = Integer.parseInt(numberPart);
            return number >= MIN_RANDOM_NUMBER && number <= MAX_RANDOM_NUMBER;
        } catch (NumberFormatException e) {
            return false;
        }
    }
} 