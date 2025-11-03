/**
 * IP地址工具类：提供IP地址解析和地理位置查询功能
 * 
 * 核心功能：
 * 1. 获取客户端真实IP地址
 * 2. IP地址格式验证
 * 3. IP地理位置解析（预留接口）
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-14 23:45:00
 */
package com.okbug.platform.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Slf4j
public class IpUtils {
    
    /**
     * IPv4地址正则表达式
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
    );
    
    /**
     * 本地IP地址
     */
    private static final String LOCAL_IP = "127.0.0.1";
    private static final String LOCAL_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String UNKNOWN = "unknown";
    
    /**
     * IP地理位置缓存
     */
    private static final ConcurrentHashMap<String, String> LOCATION_CACHE = new ConcurrentHashMap<>();
    
    /**
     * IP地理位置查询API
     */
    private static final String IP_API_URL = "http://ip-api.com/json/";
    
    /**
     * 请求超时时间（毫秒）
     */
    private static final int REQUEST_TIMEOUT = 3000;
    
    /**
     * 获取客户端真实IP地址
     * 支持代理、负载均衡等场景下的IP获取
     * 
     * @param request HttpServletRequest对象
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return LOCAL_IP;
        }

        String ip = null;

        // 1) 优先从标准代理头中解析（支持多级代理）
        // X-Forwarded-For: client, proxy1, proxy2
        String xff = safeHeader(request, "X-Forwarded-For");
        if (StrUtil.isNotBlank(xff)) {
            String[] parts = xff.split(",");
            for (String part : parts) {
                String candidate = part == null ? null : part.trim();
                if (isValidIp(candidate)) {
                    ip = candidate;
                    break;
                }
            }
            if (isValidIp(ip)) {
                return normalizeIp(ip);
            }
        }

        // 2) 其它常见代理头
        String[] headerKeys = new String[] {
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "X-Cluster-Client-IP"
        };
        for (String key : headerKeys) {
            String v = safeHeader(request, key);
            if (isValidIp(v)) {
                return normalizeIp(v);
            }
        }

        // 3) 回退到 remote addr
        try {
            ip = request.getRemoteAddr();
        } catch (Exception e) {
            ip = null;
        }

        if (!StrUtil.isNotBlank(ip)) {
            return LOCAL_IP;
        }

        return normalizeIp(ip);
    }

    private static String normalizeIp(String ip) {
        if (ip == null) {
            return LOCAL_IP;
        }
        String v = ip.trim();
        if (LOCAL_IPV6.equals(v)) {
            v = LOCAL_IP;
        }
        // 对于明显的内网地址，直接返回原值；不再尝试 InetAddress 解析公网IP，避免阻塞
        // 如需真实公网出口IP，应在网关层注入 X-Forwarded-For
        return StrUtil.isNotBlank(v) ? v : LOCAL_IP;
    }

    private static String safeHeader(HttpServletRequest request, String name) {
        try {
            String val = request.getHeader(name);
            return val == null ? null : val.trim();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 检查IP是否有效
     * 
     * @param ip IP地址
     * @return 是否有效
     */
    private static boolean isValidIp(String ip) {
        return ip != null 
            && !ip.trim().isEmpty() 
            && !UNKNOWN.equalsIgnoreCase(ip) 
            && isValidIpFormat(ip);
    }
    
    /**
     * 验证IP地址格式
     * 
     * @param ip IP地址
     * @return 格式是否正确
     */
    public static boolean isValidIpFormat(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        
        // 检查IPv4格式
        if (IPV4_PATTERN.matcher(ip.trim()).matches()) {
            return true;
        }
        
        // 检查IPv6格式（简单检查）
        if (ip.contains(":")) {
            try {
                InetAddress.getByName(ip);
                return true;
            } catch (UnknownHostException e) {
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * 判断是否为内网IP
     * 
     * @param ip IP地址
     * @return 是否为内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return true;
        }
        
        ip = ip.trim();
        
        // IPv6本地地址
        if (LOCAL_IPV6.equals(ip)) {
            return true;
        }
        
        // IPv4本地地址
        if (LOCAL_IP.equals(ip)) {
            return true;
        }
        
        // 检查IPv4内网地址段
        if (IPV4_PATTERN.matcher(ip).matches()) {
            String[] parts = ip.split("\\.");
            int firstOctet = Integer.parseInt(parts[0]);
            int secondOctet = Integer.parseInt(parts[1]);
            
            // 10.0.0.0/8
            if (firstOctet == 10) {
                return true;
            }
            
            // 172.16.0.0/12
            if (firstOctet == 172 && secondOctet >= 16 && secondOctet <= 31) {
                return true;
            }
            
            // 192.168.0.0/16
            if (firstOctet == 192 && secondOctet == 168) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取IP地理位置信息
     * 集成免费IP-API服务进行地理位置查询
     * 
     * @param ip IP地址
     * @return 地理位置信息
     */
    public static String getLocationByIp(String ip) {
        if (StrUtil.isBlank(ip)) {
            return "未知";
        }
        
        ip = ip.trim();
        
        // 内网IP直接返回内网
        if (isInternalIp(ip)) {
            return "内网IP";
        }
        
        // 先从缓存获取
        String cachedLocation = LOCATION_CACHE.get(ip);
        if (StrUtil.isNotBlank(cachedLocation)) {
            return cachedLocation;
        }
        
        // 调用IP地理位置查询API
        String location = queryLocationFromApi(ip);
        
        // 缓存结果
        if (StrUtil.isNotBlank(location)) {
            LOCATION_CACHE.put(ip, location);
        }
        
        return location;
    }
    
    /**
     * 从API查询IP地理位置信息
     * 
     * @param ip IP地址
     * @return 地理位置信息
     */
    private static String queryLocationFromApi(String ip) {
        try {
            // 构建请求URL
            String url = IP_API_URL + ip + "?lang=zh-CN&fields=status,country,regionName,city";
            
            // 发起HTTP请求
            String response = HttpUtil.get(url, REQUEST_TIMEOUT);
            
            if (StrUtil.isBlank(response)) {
                log.warn("IP地理位置查询响应为空，IP: {}", ip);
                return "未知地区";
            }
            
            // 解析JSON响应
            JSONObject jsonObject = JSONUtil.parseObj(response);
            String status = jsonObject.getStr("status");
            
            if (!"success".equals(status)) {
                log.warn("IP地理位置查询失败，IP: {}, 状态: {}", ip, status);
                return "未知地区";
            }
            
            // 构建地理位置信息
            String country = jsonObject.getStr("country", "");
            String region = jsonObject.getStr("regionName", "");
            String city = jsonObject.getStr("city", "");
            
            StringBuilder location = new StringBuilder();
            
            if (StrUtil.isNotBlank(country) && !"中国".equals(country)) {
                location.append(country);
            }
            
            if (StrUtil.isNotBlank(region)) {
                if (location.length() > 0) {
                    location.append("-");
                }
                location.append(region);
            }
            
            if (StrUtil.isNotBlank(city) && !city.equals(region)) {
                if (location.length() > 0) {
                    location.append("-");
                }
                location.append(city);
            }
            
            String result = location.length() > 0 ? location.toString() : "未知地区";
            log.debug("IP地理位置查询成功，IP: {}, 位置: {}", ip, result);
            
            return result;
            
        } catch (Exception e) {
            log.warn("IP地理位置查询异常，IP: {}", ip, e);
            return "未知地区";
        }
    }
    
    /**
     * 获取城市信息（兼容老接口）
     * 
     * @param ip IP地址
     * @return 城市信息
     */
    public static String getCityInfo(String ip) {
        return getLocationByIp(ip);
    }
    
    /**
     * 隐藏IP地址的部分信息（用于日志显示）
     * 
     * @param ip 原始IP地址
     * @return 隐藏部分信息的IP地址
     */
    public static String maskIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return "未知";
        }
        
        ip = ip.trim();
        
        // IPv4地址隐藏
        if (IPV4_PATTERN.matcher(ip).matches()) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + ".***." + parts[3];
            }
        }
        
        // IPv6地址隐藏
        if (ip.contains(":")) {
            if (ip.length() > 8) {
                return ip.substring(0, 4) + ":***:" + ip.substring(ip.length() - 4);
            }
        }
        
        return ip;
    }
} 