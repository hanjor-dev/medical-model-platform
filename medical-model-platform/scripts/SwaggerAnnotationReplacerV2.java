import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

/**
 * Swagger 2.x → 3.x 注解替换工具 V2
 * 正确处理UTF-8编码，避免中文乱码
 * 
 * 使用方法：
 * javac -encoding UTF-8 SwaggerAnnotationReplacerV2.java
 * java SwaggerAnnotationReplacerV2
 */
public class SwaggerAnnotationReplacerV2 {
    
    private static int totalFiles = 0;
    private static int modifiedFiles = 0;
    private static int errorFiles = 0;
    
    public static void main(String[] args) {
        String projectRoot = System.getProperty("user.dir");
        System.out.println("🚀 开始Swagger注解替换 (UTF-8安全版本)");
        System.out.println("📂 项目根目录: " + projectRoot);
        System.out.println("=" .repeat(80));
        
        try {
            // 1. 处理 Controller 文件
            System.out.println("\n📁 处理 Controller 文件...");
            processDirectory(Paths.get(projectRoot, "src/main/java/com/okbug/platform/controller"));
            
            // 2. 处理 DTO 文件
            System.out.println("\n📁 处理 DTO 文件...");
            processDirectory(Paths.get(projectRoot, "src/main/java/com/okbug/platform/dto"));
            
            // 3. 处理 VO 文件
            System.out.println("\n📁 处理 VO 文件...");
            processDirectory(Paths.get(projectRoot, "src/main/java/com/okbug/platform/vo"));
            
            // 打印统计信息
            System.out.println("\n" + "=".repeat(80));
            System.out.println("✅ 替换完成！");
            System.out.println("📊 统计信息:");
            System.out.println("   - 扫描文件: " + totalFiles);
            System.out.println("   - 修改文件: " + modifiedFiles);
            System.out.println("   - 错误文件: " + errorFiles);
            System.out.println("=" .repeat(80));
            
        } catch (Exception e) {
            System.err.println("❌ 发生错误: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 递归处理目录
     */
    private static void processDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            System.out.println("⚠️  目录不存在: " + directory);
            return;
        }
        
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                 .forEach(SwaggerAnnotationReplacerV2::processFile);
        }
    }
    
    /**
     * 处理单个文件
     */
    private static void processFile(Path file) {
        totalFiles++;
        
        try {
            // 使用 UTF-8 读取文件
            String content = Files.readString(file, StandardCharsets.UTF_8);
            String originalContent = content;
            
            // 执行替换
            content = replaceImports(content);
            content = replaceAnnotations(content);
            
            // 如果内容有变化，保存文件
            if (!content.equals(originalContent)) {
                // 使用 UTF-8 写入文件
                Files.writeString(file, content, StandardCharsets.UTF_8);
                modifiedFiles++;
                System.out.println("✅ " + file.getFileName());
            }
            
        } catch (Exception e) {
            errorFiles++;
            System.err.println("❌ 处理失败: " + file.getFileName() + " - " + e.getMessage());
        }
    }
    
    /**
     * 替换 import 语句
     */
    private static String replaceImports(String content) {
        Map<String, String> importMappings = new LinkedHashMap<>();
        
        // Swagger 2.x → 3.x import 映射
        importMappings.put("import io.swagger.annotations.Api;", 
                          "import io.swagger.v3.oas.annotations.tags.Tag;");
        importMappings.put("import io.swagger.annotations.ApiOperation;", 
                          "import io.swagger.v3.oas.annotations.Operation;");
        importMappings.put("import io.swagger.annotations.ApiParam;", 
                          "import io.swagger.v3.oas.annotations.Parameter;");
        importMappings.put("import io.swagger.annotations.ApiModel;", 
                          "import io.swagger.v3.oas.annotations.media.Schema;");
        importMappings.put("import io.swagger.annotations.ApiModelProperty;", 
                          "import io.swagger.v3.oas.annotations.media.Schema;");
        importMappings.put("import io.swagger.annotations.ApiImplicitParam;", 
                          "import io.swagger.v3.oas.annotations.Parameter;");
        importMappings.put("import io.swagger.annotations.ApiImplicitParams;", 
                          "import io.swagger.v3.oas.annotations.Parameters;");
        
        for (Map.Entry<String, String> entry : importMappings.entrySet()) {
            content = content.replace(entry.getKey(), entry.getValue());
        }
        
        // 移除重复的 import
        content = removeDuplicateImports(content);
        
        return content;
    }
    
    /**
     * 替换注解
     */
    private static String replaceAnnotations(String content) {
        // 1. @Api → @Tag
        content = content.replaceAll(
            "@Api\\(tags\\s*=\\s*\"([^\"]+)\"\\)",
            "@Tag(name = \"$1\")"
        );
        
        // 2. @ApiOperation → @Operation (处理 value 和 notes)
        content = content.replaceAll(
            "@ApiOperation\\(value\\s*=\\s*\"([^\"]+)\"\\s*,\\s*notes\\s*=\\s*\"([^\"]+)\"\\)",
            "@Operation(summary = \"$1\", description = \"$2\")"
        );
        content = content.replaceAll(
            "@ApiOperation\\(value\\s*=\\s*\"([^\"]+)\"\\)",
            "@Operation(summary = \"$1\")"
        );
        content = content.replaceAll(
            "@ApiOperation\\(\"([^\"]+)\"\\)",
            "@Operation(summary = \"$1\")"
        );
        
        // 3. @ApiParam → @Parameter
        content = content.replaceAll(
            "@ApiParam\\(value\\s*=\\s*\"([^\"]+)\"",
            "@Parameter(description = \"$1\""
        );
        content = content.replaceAll(
            "@ApiParam\\(\"([^\"]+)\"\\)",
            "@Parameter(description = \"$1\")"
        );
        
        // 4. @ApiModel → @Schema
        content = content.replaceAll(
            "@ApiModel\\(value\\s*=\\s*\"([^\"]+)\"\\s*,\\s*description\\s*=\\s*\"([^\"]+)\"\\)",
            "@Schema(name = \"$1\", description = \"$2\")"
        );
        content = content.replaceAll(
            "@ApiModel\\(value\\s*=\\s*\"([^\"]+)\"\\)",
            "@Schema(name = \"$1\")"
        );
        content = content.replaceAll(
            "@ApiModel\\(\"([^\"]+)\"\\)",
            "@Schema(name = \"$1\")"
        );
        
        // 5. @ApiModelProperty → @Schema
        content = content.replaceAll(
            "@ApiModelProperty\\(value\\s*=\\s*\"([^\"]+)\"",
            "@Schema(description = \"$1\""
        );
        content = content.replaceAll(
            "@ApiModelProperty\\(\"([^\"]+)\"\\)",
            "@Schema(description = \"$1\")"
        );
        
        return content;
    }
    
    /**
     * 移除重复的 import 语句
     */
    private static String removeDuplicateImports(String content) {
        String[] lines = content.split("\n");
        Set<String> seenImports = new LinkedHashSet<>();
        StringBuilder result = new StringBuilder();
        
        boolean inImportSection = false;
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            if (trimmed.startsWith("import ")) {
                inImportSection = true;
                if (!seenImports.contains(trimmed)) {
                    seenImports.add(trimmed);
                    result.append(line).append("\n");
                }
            } else {
                if (inImportSection && trimmed.isEmpty()) {
                    // import区域结束
                    inImportSection = false;
                }
                result.append(line).append("\n");
            }
        }
        
        return result.toString();
    }
}

