import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Swagger注解替换工具
 * 使用Java处理文件，确保UTF-8编码正确
 * 
 * @author AI Assistant
 * @date 2025-10-31
 */
public class SwaggerAnnotationReplacer {
    
    private static int fileCount = 0;
    private static int replaceCount = 0;
    
    public static void main(String[] args) throws IOException {
        String projectRoot = "D:\\01_Project\\02_Personal\\model-platform-project\\medical-model-platform";
        
        System.out.println("=== Swagger注解替换工具 ===");
        System.out.println("项目根目录: " + projectRoot);
        System.out.println();
        
        // 处理所有Java文件
        Path srcPath = Paths.get(projectRoot, "src", "main", "java");
        
        System.out.println("开始处理文件...");
        System.out.println();
        
        processDirectory(srcPath);
        
        System.out.println();
        System.out.println("=== 处理完成 ===");
        System.out.println("共处理文件: " + fileCount);
        System.out.println("共替换次数: " + replaceCount);
    }
    
    private static void processDirectory(Path dir) throws IOException {
        Files.walk(dir)
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(SwaggerAnnotationReplacer::processFile);
    }
    
    private static void processFile(Path file) {
        try {
            // 读取文件内容（UTF-8）
            String content = Files.readString(file, StandardCharsets.UTF_8);
            String originalContent = content;
            
            // 统计本文件替换次数
            int fileReplaceCount = 0;
            
            // 1. 替换import语句
            content = content.replaceAll("import io\\.swagger\\.annotations\\.Api;", "import io.swagger.v3.oas.annotations.tags.Tag;");
            content = content.replaceAll("import io\\.swagger\\.annotations\\.ApiOperation;", "import io.swagger.v3.oas.annotations.Operation;");
            content = content.replaceAll("import io\\.swagger\\.annotations\\.ApiParam;", "import io.swagger.v3.oas.annotations.Parameter;");
            content = content.replaceAll("import io\\.swagger\\.annotations\\.ApiModel;", "import io.swagger.v3.oas.annotations.media.Schema;");
            content = content.replaceAll("import io\\.swagger\\.annotations\\.ApiModelProperty;", "import io.swagger.v3.oas.annotations.media.Schema;");
            
            // 2. 替换@Api注解
            Pattern apiPattern = Pattern.compile("@Api\\s*\\(\\s*tags\\s*=\\s*\"([^\"]+)\"\\s*\\)", Pattern.MULTILINE);
            Matcher apiMatcher = apiPattern.matcher(content);
            StringBuffer sb = new StringBuffer();
            while (apiMatcher.find()) {
                apiMatcher.appendReplacement(sb, "@Tag(name = \"" + apiMatcher.group(1) + "\")");
                fileReplaceCount++;
            }
            apiMatcher.appendTail(sb);
            content = sb.toString();
            
            // 3. 替换@ApiOperation注解的notes为description
            content = content.replaceAll(
                "@ApiOperation\\s*\\(\\s*value\\s*=",
                "@Operation(summary ="
            );
            
            // 复杂替换：notes -> description
            Pattern notesPattern = Pattern.compile(
                "@Operation\\s*\\(\\s*summary\\s*=\\s*\"([^\"]+)\"\\s*,\\s*notes\\s*=\\s*\"([^\"]+)\"\\s*\\)",
                Pattern.MULTILINE
            );
            Matcher notesMatcher = notesPattern.matcher(content);
            sb = new StringBuffer();
            while (notesMatcher.find()) {
                notesMatcher.appendReplacement(sb, 
                    "@Operation(summary = \"" + notesMatcher.group(1) + "\", description = \"" + notesMatcher.group(2) + "\")"
                );
                fileReplaceCount++;
            }
            notesMatcher.appendTail(sb);
            content = sb.toString();
            
            // 4. 替换@ApiParam注解
            content = content.replaceAll(
                "@ApiParam\\s*\\(\\s*value\\s*=",
                "@Parameter(description ="
            );
            content = content.replaceAll(
                "@ApiParam\\s*\\(\\s*name\\s*=",
                "@Parameter(name ="
            );
            
            // 5. 替换@ApiModel和@ApiModelProperty
            content = content.replaceAll(
                "@ApiModel\\s*\\(\\s*value\\s*=",
                "@Schema(name ="
            );
            content = content.replaceAll(
                "@ApiModel\\s*\\(\\s*description\\s*=",
                "@Schema(description ="
            );
            content = content.replaceAll(
                "@ApiModelProperty\\s*\\(\\s*value\\s*=",
                "@Schema(description ="
            );
            
            // 6. 清理多余的import（如果文件中不再使用）
            if (!content.contains("io.swagger.annotations")) {
                content = removeUnusedImports(content);
            }
            
            // 如果内容有变化，写回文件
            if (!content.equals(originalContent)) {
                Files.writeString(file, content, StandardCharsets.UTF_8);
                fileCount++;
                replaceCount += fileReplaceCount;
                System.out.println("✓ " + file.getFileName() + " (替换 " + fileReplaceCount + " 处)");
            }
            
        } catch (IOException e) {
            System.err.println("✗ 处理文件失败: " + file);
            e.printStackTrace();
        }
    }
    
    private static String removeUnusedImports(String content) {
        // 移除未使用的Swagger 2.x import
        content = content.replaceAll("import io\\.swagger\\.annotations\\.\\*;\\s*\n", "");
        content = content.replaceAll("import io\\.swagger\\.annotations\\.[^;]+;\\s*\n", "");
        return content;
    }
}

