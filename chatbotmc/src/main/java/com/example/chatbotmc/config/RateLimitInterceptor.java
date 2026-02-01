package com.example.chatbotmc.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting interceptor to prevent API abuse
 * Limits requests per IP address using token bucket algorithm
 * Stricter limits for authentication endpoints to prevent brute force attacks
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    // Separate caches for different rate limit tiers
    private final Map<String, Bucket> generalCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> authCache = new ConcurrentHashMap<>();
    
    // Default: 100 requests per minute per IP
    private static final int REQUESTS_PER_MINUTE = 100;
    
    // Auth endpoints: 1 request per minute per IP (stricter to prevent brute force)
    private static final int AUTH_REQUESTS_PER_MINUTE = 1;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIP = getClientIP(request);
        String requestURI = request.getRequestURI();
        
        // Use stricter rate limiting for authentication endpoints
        if (isAuthEndpoint(requestURI)) {
            return handleAuthRateLimit(clientIP, response);
        }
        
        // General rate limiting for other endpoints
        return handleGeneralRateLimit(clientIP, response);
    }
    
    /**
     * Check if the request is to an authentication endpoint
     */
    private boolean isAuthEndpoint(String uri) {
        return uri != null && (
            uri.contains("/api/auth/login") || 
            uri.contains("/api/auth/register")
        );
    }
    
    private boolean handleAuthRateLimit(String clientIP, HttpServletResponse response) throws Exception {
        String key = "auth:" + clientIP;
        Bucket bucket = authCache.computeIfAbsent(key, k -> createAuthBucket());
        
        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Too many login attempts. Please try again in a few minutes.\"}");
            return false;
        }
    }
    
    private boolean handleGeneralRateLimit(String clientIP, HttpServletResponse response) throws Exception {
        Bucket bucket = generalCache.computeIfAbsent(clientIP, k -> createGeneralBucket());
        
        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Too many requests. Please try again later.\"}");
            return false;
        }
    }
    
    private Bucket createGeneralBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(REQUESTS_PER_MINUTE)
            .refillGreedy(REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
            .build();
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
    
    private Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(AUTH_REQUESTS_PER_MINUTE)
            .refillGreedy(AUTH_REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
            .build();
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
    
    /**
     * Extract client IP address from request
     * Checks multiple headers and validates against spoofing
     * For production behind proxy: configure trusted proxy IPs
     */
    private String getClientIP(HttpServletRequest request) {
        // Check X-Forwarded-For header (set by proxies/load balancers)
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            // Take the rightmost IP that isn't from a private range
            // This assumes your load balancer adds the real client IP
            String[] ips = xfHeader.split(",");
            for (int i = ips.length - 1; i >= 0; i--) {
                String ip = ips[i].trim();
                if (!ip.isEmpty() && !isPrivateIP(ip)) {
                    return ip;
                }
            }
        }
        
        // Check X-Real-IP header (alternative header)
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        // Fallback to remote address
        return request.getRemoteAddr();
    }
    
    /**
     * Check if IP is from private/internal range
     * Used to prevent spoofing via X-Forwarded-For
     */
    private boolean isPrivateIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return true;
        }
        
        // Simple check for common private ranges
        return ip.startsWith("10.") || 
               ip.startsWith("192.168.") || 
               ip.startsWith("172.16.") || 
               ip.startsWith("172.17.") || 
               ip.startsWith("172.18.") || 
               ip.startsWith("172.19.") || 
               ip.startsWith("172.20.") || 
               ip.startsWith("172.21.") || 
               ip.startsWith("172.22.") || 
               ip.startsWith("172.23.") || 
               ip.startsWith("172.24.") || 
               ip.startsWith("172.25.") || 
               ip.startsWith("172.26.") || 
               ip.startsWith("172.27.") || 
               ip.startsWith("172.28.") || 
               ip.startsWith("172.29.") || 
               ip.startsWith("172.30.") || 
               ip.startsWith("172.31.") ||
               ip.equals("127.0.0.1") ||
               ip.equals("::1") ||
               ip.equals("0:0:0:0:0:0:0:1");
    }
}
