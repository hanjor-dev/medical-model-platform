package com.okbug.platform.dto.permission.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "用户权限覆盖批量更新请求")
public class UserPermissionOverrideUpdateRequest {

    @NotNull
    @Schema(description = "用户ID", required = true, example = "1001")
    private Long userId;

    @Schema(description = "显式允许的权限ID列表")
    private List<Long> allowPermissionIds;

    @Schema(description = "显式拒绝的权限ID列表（已不使用）")
    private List<Long> denyPermissionIds;
}


