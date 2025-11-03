/**
 * 用户代理工具类：提供User-Agent解析功能
 * 
 * 核心功能：
 * 1. 解析浏览器类型和版本
 * 2. 解析操作系统信息
 * 3. 解析设备类型（桌面、移动、平板）
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-14 23:50:00
 */
package com.okbug.platform.common.utils;

import jakarta.servlet.http.HttpServletRequest;

public class UserAgentUtils {
    
    /**
     * 用户代理信息封装类
     */
    public static class UserAgentInfo {
        private String browser;           // 浏览器
        private String browserVersion;    // 浏览器版本
        private String os;               // 操作系统
        private String deviceType;       // 设备类型
        private String originalAgent;    // 原始User-Agent
        
        public UserAgentInfo(String originalAgent) {
            this.originalAgent = originalAgent;
        }
        
        // Getter和Setter方法
        public String getBrowser() { return browser; }
        public void setBrowser(String browser) { this.browser = browser; }
        
        public String getBrowserVersion() { return browserVersion; }
        public void setBrowserVersion(String browserVersion) { this.browserVersion = browserVersion; }
        
        public String getOs() { return os; }
        public void setOs(String os) { this.os = os; }
        
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        
        public String getOriginalAgent() { return originalAgent; }
        public void setOriginalAgent(String originalAgent) { this.originalAgent = originalAgent; }
        
        @Override
        public String toString() {
            return String.format("%s %s on %s (%s)", 
                browser != null ? browser : "Unknown", 
                browserVersion != null ? browserVersion : "", 
                os != null ? os : "Unknown OS", 
                deviceType != null ? deviceType : "Unknown Device");
        }
    }
    
    /**
     * 从HttpServletRequest获取User-Agent
     * 
     * @param request HTTP请求对象
     * @return User-Agent字符串
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "Unknown";
        }
        
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown";
    }
    
    /**
     * 解析User-Agent信息
     * 
     * @param userAgent User-Agent字符串
     * @return 解析后的用户代理信息
     */
    public static UserAgentInfo parseUserAgent(String userAgent) {
        UserAgentInfo info = new UserAgentInfo(userAgent);
        
        if (userAgent == null || userAgent.trim().isEmpty()) {
            info.setBrowser("Unknown");
            info.setOs("Unknown");
            info.setDeviceType("Unknown");
            return info;
        }
        
        String agent = userAgent.toLowerCase();
        
        // 解析浏览器信息
        parseBrowser(agent, info);
        
        // 解析操作系统信息
        parseOperatingSystem(agent, info);
        
        // 解析设备类型
        parseDeviceType(agent, info);
        
        return info;
    }
    
    /**
     * 从HttpServletRequest解析User-Agent信息
     * 
     * @param request HTTP请求对象
     * @return 解析后的用户代理信息
     */
    public static UserAgentInfo parseUserAgent(HttpServletRequest request) {
        String userAgent = getUserAgent(request);
        return parseUserAgent(userAgent);
    }
    
    /**
     * 解析浏览器信息
     */
    private static void parseBrowser(String agent, UserAgentInfo info) {
        if (agent.contains("edg/") || agent.contains("edge/")) {
            info.setBrowser("Microsoft Edge");
            extractVersion(agent, "edg/", info);
        } else if (agent.contains("chrome/") && !agent.contains("chromium/")) {
            info.setBrowser("Google Chrome");
            extractVersion(agent, "chrome/", info);
        } else if (agent.contains("firefox/")) {
            info.setBrowser("Mozilla Firefox");
            extractVersion(agent, "firefox/", info);
        } else if (agent.contains("safari/") && !agent.contains("chrome/")) {
            info.setBrowser("Safari");
            extractVersion(agent, "version/", info);
        } else if (agent.contains("opera/") || agent.contains("opr/")) {
            info.setBrowser("Opera");
            if (agent.contains("opr/")) {
                extractVersion(agent, "opr/", info);
            } else {
                extractVersion(agent, "opera/", info);
            }
        } else if (agent.contains("msie") || agent.contains("trident/")) {
            info.setBrowser("Internet Explorer");
            if (agent.contains("msie")) {
                extractVersion(agent, "msie ", info);
            } else {
                extractVersion(agent, "rv:", info);
            }
        } else {
            info.setBrowser("Unknown Browser");
        }
    }
    
    /**
     * 解析操作系统信息
     */
    private static void parseOperatingSystem(String agent, UserAgentInfo info) {
        if (agent.contains("windows nt 10") || agent.contains("windows nt 11")) {
            info.setOs("Windows 10/11");
        } else if (agent.contains("windows nt 6.3")) {
            info.setOs("Windows 8.1");
        } else if (agent.contains("windows nt 6.2")) {
            info.setOs("Windows 8");
        } else if (agent.contains("windows nt 6.1")) {
            info.setOs("Windows 7");
        } else if (agent.contains("windows nt")) {
            info.setOs("Windows");
        } else if (agent.contains("mac os x") || agent.contains("macos")) {
            info.setOs("macOS");
        } else if (agent.contains("linux")) {
            if (agent.contains("android")) {
                info.setOs("Android");
            } else {
                info.setOs("Linux");
            }
        } else if (agent.contains("iphone") || agent.contains("ipad")) {
            info.setOs("iOS");
        } else {
            info.setOs("Unknown OS");
        }
    }
    
    /**
     * 解析设备类型
     */
    private static void parseDeviceType(String agent, UserAgentInfo info) {
        if (agent.contains("mobile") || agent.contains("android") || agent.contains("iphone")) {
            info.setDeviceType("Mobile");
        } else if (agent.contains("ipad") || agent.contains("tablet")) {
            info.setDeviceType("Tablet");
        } else {
            info.setDeviceType("Desktop");
        }
    }
    
    /**
     * 提取版本号
     */
    private static void extractVersion(String agent, String versionPrefix, UserAgentInfo info) {
        int startIndex = agent.indexOf(versionPrefix);
        if (startIndex != -1) {
            startIndex += versionPrefix.length();
            int endIndex = agent.indexOf(' ', startIndex);
            if (endIndex == -1) {
                endIndex = agent.indexOf(';', startIndex);
            }
            if (endIndex == -1) {
                endIndex = agent.indexOf(')', startIndex);
            }
            if (endIndex == -1) {
                endIndex = agent.length();
            }
            
            if (startIndex < endIndex && endIndex <= agent.length()) {
                String version = agent.substring(startIndex, endIndex);
                // 只取主版本号（第一个点之前的部分）
                int dotIndex = version.indexOf('.');
                if (dotIndex > 0) {
                    version = version.substring(0, dotIndex + 2); // 保留一位小数
                    if (version.endsWith(".")) {
                        version = version.substring(0, version.length() - 1);
                    }
                }
                info.setBrowserVersion(version);
            }
        }
    }
    
    /**
     * 判断是否为移动设备
     * 
     * @param userAgent User-Agent字符串
     * @return 是否为移动设备
     */
    public static boolean isMobileDevice(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return false;
        }
        
        String agent = userAgent.toLowerCase();
        return agent.contains("mobile") || 
               agent.contains("android") || 
               agent.contains("iphone") ||
               agent.contains("windows phone");
    }
    
    /**
     * 判断是否为移动设备
     * 
     * @param request HTTP请求对象
     * @return 是否为移动设备
     */
    public static boolean isMobileDevice(HttpServletRequest request) {
        return isMobileDevice(getUserAgent(request));
    }
} 