import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.regex.*;

/**
 * 修复 schema = @Schema(allowableValues) 的问题
 * 在OpenAPI 3.0中，@Schema不应该嵌套使用
 * 
 * 修复策略：移除嵌套的 schema = @Schema(...) 部分
 */
public class FixSchemaAllowableValues {
    
    public static void main(String[] args) {
        String projectRoot = System.getProperty("user.dir");
        System.out.println("🔧 修复schema嵌套问题");
        
        String[] files = {
            "src/main/java/com/okbug/platform/dto/auth/response/LoginPermissionInfo.java",
            "src/main/java/com/okbug/platform/controller/permission/PermissionManagementController.java",
            "src/main/java/com/okbug/platform/dto/permission/request/RolePermissionUpdateRequest.java",
            "src/main/java/com/okbug/platform/dto/permission/request/PermissionCreateRequest.java",
            "src/main/java/com/okbug/platform/dto/permission/request/PermissionQueryRequest.java",
            "src/main/java/com/okbug/platform/dto/team/SetTeamStatusRequest.java",
            "src/main/java/com/okbug/platform/dto/team/UpdateTeamRequest.java"
        };
        
        for (String file : files) {
            Path filePath = Paths.get(projectRoot, file);
            try {
                String content = Files.readString(filePath, StandardCharsets.UTF_8);
                String original = content;
                
                // 修复方案1: 移除 , schema = @Schema(allowableValues = {...})
                content = content.replaceAll(
                    ",\\s*schema\\s*=\\s*@Schema\\(allowableValues\\s*=\\s*\\{\"([^\"]+)\"\\}\\)",
                    ""
                );
                
                // 修复方案2: 如果第一个参数就是schema，整体移除
                content = content.replaceAll(
                    "schema\\s*=\\s*@Schema\\(allowableValues\\s*=\\s*\\{\"([^\"]+)\"\\}\\)\\s*,\\s*",
                    ""
                );
                
                if (!content.equals(original)) {
                    Files.writeString(filePath, content, StandardCharsets.UTF_8);
                    System.out.println("✅ " + Paths.get(file).getFileName());
                }
                
            } catch (Exception e) {
                System.err.println("❌ " + file + ": " + e.getMessage());
            }
        }
        
        System.out.println("✅ 修复完成！");
    }
}

