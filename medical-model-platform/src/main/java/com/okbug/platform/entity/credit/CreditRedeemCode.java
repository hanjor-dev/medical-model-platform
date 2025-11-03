package com.okbug.platform.entity.credit;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 兑换码实体：对应表 credit_redeem_codes
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("credit_redeem_codes")
public class CreditRedeemCode {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String codeKey;

    private String creditTypeCode;

    private BigDecimal amount;

    /**
     * 0:待使用 1:已兑换 2:已失效 3:已作废
     */
    private Integer status;

    private LocalDateTime expireTime;

    private String remark;

    private Long createdBy;

    private String createdByName;

    private Long redeemedBy;

    private String redeemedByName;

    private LocalDateTime redeemedTime;

    @Version
    private Integer version;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}


