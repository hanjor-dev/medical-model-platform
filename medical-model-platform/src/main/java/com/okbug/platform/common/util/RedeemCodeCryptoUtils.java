package com.okbug.platform.common.util;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.common.base.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 兑换码加密/签名工具
 * 说明：采用 HMAC-SHA256 对明文载荷签名，输出 Base64Url 编码串
 * 明文载荷格式：v1|type|amount|expire|nonce
 */
@Slf4j
public class RedeemCodeCryptoUtils {

    private static final String HMAC_ALG = "HmacSHA256";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * 生成签名（Base64Url）
     *
     * @param secret HMAC密钥
     * @param creditTypeCode 积分类别编码
     * @param amount 积分金额
     * @param expireTime 过期时间（可空）
     * @param nonce 随机串（可空）
     * @return Base64Url编码的签名
     */
    public static String generate(String secret, String creditTypeCode, BigDecimal amount, LocalDateTime expireTime, String nonce) {
        String payload = buildPayload(creditTypeCode, amount, expireTime, nonce);
        String signature = hmacBase64Url(secret, payload);
        log.debug("Redeem signature generated. Payload length: {}", payload.length());
        return signature;
    }

    /**
     * 校验签名
     *
     * @param secret HMAC密钥
     * @param codeKey 待校验签名
     * @param creditTypeCode 积分类别编码
     * @param amount 积分金额
     * @param expireTime 过期时间（可空）
     * @param nonce 随机串（可空）
     * @return 是否匹配
     */
    public static boolean verify(String secret, String codeKey, String creditTypeCode, BigDecimal amount, LocalDateTime expireTime, String nonce) {
        String payload = buildPayload(creditTypeCode, amount, expireTime, nonce);
        String expected = hmacBase64Url(secret, payload);
        boolean ok = constantTimeEquals(codeKey, expected);
        log.debug("Redeem signature verify result: {}", ok);
        return ok;
    }

    private static String buildPayload(String creditTypeCode, BigDecimal amount, LocalDateTime expireTime, String nonce) {
        String expire = expireTime == null ? "" : expireTime.format(FMT);
        return String.join("|", "v1", nullSafe(creditTypeCode), amount.stripTrailingZeros().toPlainString(), expire, nullSafe(nonce));
    }

    private static String hmacBase64Url(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALG));
            byte[] sig = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
        } catch (Exception e) {
            // 与规范对齐：服务异常统一抛出并由全局异常处理器捕获
            throw new ServiceException(ErrorCode.EXTERNAL_SERVICE_ERROR, "HMAC 计算失败: " + e.getMessage());
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int res = 0;
        for (int i = 0; i < a.length(); i++) {
            res |= a.charAt(i) ^ b.charAt(i);
        }
        return res == 0;
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}


