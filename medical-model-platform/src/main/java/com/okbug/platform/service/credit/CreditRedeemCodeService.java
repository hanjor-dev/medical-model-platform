package com.okbug.platform.service.credit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.entity.credit.CreditRedeemCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CreditRedeemCodeService {

    /**
     * 生成兑换码
     */
    CreditRedeemCode generate(String creditTypeCode, BigDecimal amount, LocalDateTime expireTime, String remark);

    /**
     * 分页查询兑换码
     */
    IPage<CreditRedeemCode> page(Page<CreditRedeemCode> page, String keyword, Integer status);

    /**
     * 使用兑换码（给当前登录用户发放积分并更新状态）
     */
    boolean redeem(String codeKey);

    /**
     * 查询兑换码信息（预览）：校验有效性（未删除、未过期、未使用、状态可用）
     */
    CreditRedeemCode getInfo(String codeKey);
}


