package com.okbug.platform.dto.credit.request;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.entity.auth.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;

/**
 * 用户积分分页查询请求：用于管理员分页查询用户及其积分余额汇总
 */
@Data
@Schema(name = "用户积分分页查询请求", description = "管理员分页查询用户及其积分余额汇总")
public class UserCreditsPageRequest {

    @Schema(description = "用户名/昵称/ID关键词", example = "alice")
    private String keyword;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小不能小于1")
    private long size = 10L;

    @Schema(description = "当前页码", example = "1")
    @Min(value = 1, message = "当前页码不能小于1")
    private long current = 1L;

    public Page<User> toPage() {
        return new Page<>(current, size);
    }
}


