/**
 * 引荐服务接口：定义引荐码相关的业务操作
 * 
 * 核心功能：
 * 1. 获取用户引荐码信息
 * 2. 验证引荐码有效性
 * 3. 生成引荐链接
 * 4. 管理引荐关系
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 15:35:00
 */
package com.okbug.platform.service.referral;

import com.okbug.platform.dto.referral.ReferralCodeResponse;
import com.okbug.platform.dto.referral.ReferralValidationResult;
import com.okbug.platform.dto.referral.ReferralLinkResult;
import com.okbug.platform.dto.referral.ReferralUserResponse;

import java.util.List;

/**
 * 引荐服务接口
 * 
 * 提供引荐码管理、引荐链接生成、引荐用户查询等功能
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 16:00:00
 */
public interface ReferralService {

    /**
     * 获取当前登录用户的引荐码信息
     * 
     * @return 引荐码信息响应对象
     */
    ReferralCodeResponse getCurrentUserReferralCode();

    /**
     * 获取引荐码基础链接配置
     * 
     * @return 引荐码基础链接
     */
    String getReferralBaseUrl();
    
    /**
     * 获取当前用户的引荐用户列表
     * 
     * @param page 页码，从1开始
     * @param size 每页大小
     * @return 引荐用户列表
     */
    List<ReferralUserResponse> getCurrentUserReferralUsers(Integer page, Integer size);
} 