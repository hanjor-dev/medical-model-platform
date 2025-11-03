package com.okbug.platform.dto.permission.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "用户权限覆盖响应")
public class UserPermissionOverrideResponse {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "显式允许的权限ID列表")
    private List<Long> allowPermissionIds;

    @Schema(description = "显式拒绝的权限ID列表（已不使用，恒为空）")
    private List<Long> denyPermissionIds;

    @Schema(description = "基线权限ID（来自角色或贡献表），用于前端禁用勾选")
    private List<Long> baselinePermissionIds;

    @Schema(description = "需要锁定的父级权限ID（当子项包含基线时），用于一级禁用")
    private List<Long> lockedParentPermissionIds;

    public static UserPermissionOverrideResponse of(Long userId, List<Long> allow, List<Long> deny,
                                                    List<Long> baseline, List<Long> lockedParents) {
        UserPermissionOverrideResponse r = new UserPermissionOverrideResponse();
        r.setUserId(userId);
        r.setAllowPermissionIds(allow);
        r.setDenyPermissionIds(deny);
        r.setBaselinePermissionIds(baseline);
        r.setLockedParentPermissionIds(lockedParents);
        return r;
    }
}


