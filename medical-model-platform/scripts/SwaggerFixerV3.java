import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Swagger 注解修复工具 V3 - 处理遗漏的注解
 * 
 * 使用方法：
 * javac -encoding UTF-8 SwaggerFixerV3.java
 * java SwaggerFixerV3
 */
public class SwaggerFixerV3 {
    
    private static int totalFiles = 0;
    private static int modifiedFiles = 0;
    
    public static void main(String[] args) {
        String projectRoot = System.getProperty("user.dir");
        System.out.println("🔧 开始修复遗漏的Swagger注解");
        System.out.println("📂 项目根目录: " + projectRoot);
        System.out.println("=".repeat(80));
        
        try {
            // 处理所有 Java 文件
            processDirectory(Paths.get(projectRoot, "src/main/java/com/okbug/platform"));
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("✅ 修复完成！");
            System.out.println("📊 统计: 扫描 " + totalFiles + " 个文件，修改 " + modifiedFiles + " 个文件");
            System.out.println("=".repeat(80));
            
        } catch (Exception e) {
            System.err.println("❌ 发生错误: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void processDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        
        try (var paths = Files.walk(directory)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                 .forEach(SwaggerFixerV3::processFile);
        }
    }
    
    private static void processFile(Path file) {
        totalFiles++;
        
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            String originalContent = content;
            
            // 修复所有遗漏的注解
            content = fixAllAnnotations(content);
            
            if (!content.equals(originalContent)) {
                Files.writeString(file, content, StandardCharsets.UTF_8);
                modifiedFiles++;
                System.out.println("✅ " + file.getFileName());
            }
            
        } catch (Exception e) {
            System.err.println("❌ " + file.getFileName() + ": " + e.getMessage());
        }
    }
    
    private static String fixAllAnnotations(String content) {
        // 1. @ApiModel → @Schema (类级别)
        content = content.replaceAll(
            "@ApiModel\\(value\\s*=\\s*\"([^\"]+)\"\\s*,\\s*description\\s*=\\s*\"([^\"]+)\"\\)",
            "@Schema(name = \"$1\", description = \"$2\")"
        );
        content = content.replaceAll(
            "@ApiModel\\(description\\s*=\\s*\"([^\"]+)\"\\s*,\\s*value\\s*=\\s*\"([^\"]+)\"\\)",
            "@Schema(name = \"$2\", description = \"$1\")"
        );
        content = content.replaceAll(
            "@ApiModel\\(value\\s*=\\s*\"([^\"]+)\"\\)",
            "@Schema(name = \"$1\")"
        );
        content = content.replaceAll(
            "@ApiModel\\(description\\s*=\\s*\"([^\"]+)\"\\)",
            "@Schema(description = \"$1\")"
        );
        content = content.replaceAll("@ApiModel", "@Schema");
        
        // 2. @ApiModelProperty → @Schema (字段级别)
        // 处理完整的 notes 参数
        content = content.replaceAll(
            "@Schema\\(description\\s*=\\s*\"([^\"]+)\"\\s*,\\s*notes\\s*=\\s*\"([^\"]+)\"",
            "@Schema(description = \"$1\", title = \"$2\""
        );
        content = content.replaceAll(
            "@Schema\\(value\\s*=\\s*\"([^\"]+)\"\\s*,\\s*notes\\s*=\\s*\"([^\"]+)\"",
            "@Schema(description = \"$1\", title = \"$2\""
        );
        // 单独的 notes 参数
        content = content.replaceAll(
            ",\\s*notes\\s*=\\s*\"([^\"]+)\"",
            ", title = \"$1\""
        );
        
        // 3. @ApiOperation → @Operation (简写形式)
        content = content.replaceAll(
            "@ApiOperation\\(\"([^\"]+)\"\\)",
            "@Operation(summary = \"$1\")"
        );
        
        // 4. @Parameter allowableValues → schema
        content = content.replaceAll(
            "allowableValues\\s*=\\s*\"([^\"]+)\"",
            "schema = @Schema(allowableValues = {\"$1\"})"
        );
        
        // 5. @Api → @Tag (Controller级别 - 漏网之鱼)
        content = content.replaceAll(
            "@Api\\(tags\\s*=\\s*\"([^\"]+)\"\\s*,\\s*description\\s*=\\s*\"([^\"]+)\"\\)",
            "@Tag(name = \"$1\", description = \"$2\")"
        );
        content = content.replaceAll(
            "@Api\\(description\\s*=\\s*\"([^\"]+)\"\\s*,\\s*tags\\s*=\\s*\"([^\"]+)\"\\)",
            "@Tag(name = \"$2\", description = \"$1\")"
        );
        content = content.replaceAll(
            "@Api\\(tags\\s*=\\s*\"([^\"]+)\"\\)",
            "@Tag(name = \"$1\")"
        );
        
        // 6. @ApiOperation 的简化形式
        content = content.replaceAll(
            "@ApiOperation\\(value\\s*=\\s*\"([^\"]+)\"\\)",
            "@Operation(summary = \"$1\")"
        );
        
        // 7. @ApiParam → @Parameter
        content = content.replaceAll(
            "@ApiParam\\(value\\s*=\\s*\"([^\"]+)\"",
            "@Parameter(description = \"$1\""
        );
        content = content.replaceAll(
            "@ApiParam\\(\"([^\"]+)\"\\)",
            "@Parameter(description = \"$1\")"
        );
        
        return content;
    }
}

