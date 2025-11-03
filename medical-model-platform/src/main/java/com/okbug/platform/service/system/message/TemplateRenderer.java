package com.okbug.platform.service.system.message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * 轻量模板渲染器：无外部依赖
 *
 * 语法：
 * - 变量：{{name}}
 * - 助手：{{upper name}} {{lower name}} {{date now "yyyy-MM-dd"}}
 * - 转义：不转义，交由上层对特定场景做转义或清洗
 *
 * 安全：
 * - 提供简单的脚本标签移除 sanitizeHtml()，用于富文本模板渲染后的清洗
 */
@Component
public class TemplateRenderer {

    private static final Pattern TOKEN = Pattern.compile("\\{\\{\\s*([^}]+?)\\s*\\}}" );

    /** 简单 LRU 缓存，用于缓存渲染后的结果（按模板+varsKey） */
    private final Map<String, String> renderCache;

    public TemplateRenderer(int maxEntries) {
        this.renderCache = new LinkedHashMap<String, String>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public TemplateRenderer() {
        this(256);
    }

    /**
     * 渲染模板
     * @param template 模板文本，如：Hello {{upper name}}
     * @param variables 变量表
     * @param sanitizeHtml 是否对结果进行基本 HTML 清洗
     */
    public String render(String template, Map<String, Object> variables, boolean sanitizeHtml) {
        if (template == null) {
            return "";
        }
        Map<String, Object> vars = variables == null ? Collections.emptyMap() : variables;
        String cacheKey = buildCacheKey(template, vars);
        String cached;
        synchronized (renderCache) {
            cached = renderCache.get(cacheKey);
        }
        if (cached != null) {
            return cached;
        }

        Matcher matcher = TOKEN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String expr = matcher.group(1).trim();
            String replacement = evaluate(expr, vars);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        String result = sanitizeHtml ? sanitizeHtml(sb.toString()) : sb.toString();

        synchronized (renderCache) {
            renderCache.put(cacheKey, result);
        }
        return result;
    }

    private String evaluate(String expr, Map<String, Object> vars) {
        // helper 调用：形如  helper var "pattern"
        String[] parts = splitByWhitespace(expr);
        if (parts.length == 1) {
            return toStringSafe(resolveValue(parts[0], vars));
        }
        String helper = parts[0];
        switch (helper) {
            case "upper":
                return toStringSafe(resolveValue(parts[1], vars)).toUpperCase();
            case "lower":
                return toStringSafe(resolveValue(parts[1], vars)).toLowerCase();
            case "date":
                return renderDate(parts, vars);
            default:
                return toStringSafe(resolveValue(expr, vars));
        }
    }

    private String renderDate(String[] parts, Map<String, Object> vars) {
        // 支持：{{date now "yyyy-MM-dd"}} 或 {{date someField "yyyy-MM-dd HH:mm"}}
        if (parts.length < 2) {
            return "";
        }
        Object source;
        if ("now".equals(parts[1])) {
            source = LocalDateTime.now();
        } else {
            source = resolveValue(parts[1], vars);
        }
        String pattern = parts.length >= 3 ? unquote(parts[2]) : "yyyy-MM-dd";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
        if (source instanceof LocalDateTime) {
            return ((LocalDateTime) source).format(fmt);
        }
        if (source instanceof LocalDate) {
            return ((LocalDate) source).format(fmt);
        }
        // 其他类型尝试解析为 ISO-8601
        String s = toStringSafe(source);
        if (s.isEmpty()) return "";
        try {
            return LocalDateTime.parse(s).format(fmt);
        } catch (Exception ignore) {
        }
        try {
            return LocalDate.parse(s).format(fmt);
        } catch (Exception ignore) {
        }
        return s;
    }

    private Object resolveValue(String key, Map<String, Object> vars) {
        if (vars.containsKey(key)) {
            return vars.get(key);
        }
        return "";
    }

    private String toStringSafe(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private String[] splitByWhitespace(String expr) {
        return expr.split("\\s+");
    }

    private String unquote(String s) {
        if (s == null) return null;
        if (s.length() >= 2 && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private String buildCacheKey(String template, Map<String, Object> vars) {
        StringBuilder sb = new StringBuilder(template.length() + 64);
        sb.append(Integer.toHexString(template.hashCode())).append('#');
        // 简单拼装，注意顺序不保证：仅作为近似缓存命中
        vars.forEach((k, v) -> {
            sb.append(k).append('=').append(Objects.toString(v, "")).append('&');
        });
        return sb.toString();
    }

    /**
     * 基础 HTML 清洗：
     * - 移除 <script>...</script>
     * - 移除 onXXX= 事件处理器
     */
    public String sanitizeHtml(String html) {
        if (html == null || html.isEmpty()) return html;
        // 移除脚本块
        String cleaned = html.replaceAll("(?is)<script[^>]*>.*?</script>", "");
        // 移除事件处理器属性 on*="..."
        cleaned = cleaned.replaceAll("(?i)on[a-z]+\\s*=\\s*\"[^\"]*\"", "");
        cleaned = cleaned.replaceAll("(?i)on[a-z]+\\s*=\\s*'[^']*'", "");
        cleaned = cleaned.replaceAll("(?i)on[a-z]+\\s*=\\s*[^\\s>]+", "");
        return cleaned;
    }
}


