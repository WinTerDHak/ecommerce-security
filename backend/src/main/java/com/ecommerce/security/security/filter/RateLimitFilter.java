package com.ecommerce.security.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, RequestInfo> ipRequestCounts = new ConcurrentHashMap<>();
    private final Map<String, RequestInfo> loginAttemptCounts = new ConcurrentHashMap<>();

    private static final int GLOBAL_LIMIT = 60; // 60 requests per minute
    private static final int LOGIN_LIMIT = 5;   // 5 attempts per 15 minutes
    private static final long ONE_MINUTE = 60_000;
    private static final long FIFTEEN_MINUTES = 900_000;

    public void clear() {
        ipRequestCounts.clear();
        loginAttemptCounts.clear();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();

        if (uri.startsWith("/api/")) {
            if (!isAllowed(ipRequestCounts, ip, GLOBAL_LIMIT, ONE_MINUTE)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests");
                return;
            }
        }

        if ("/api/auth/login".equals(uri) && "POST".equalsIgnoreCase(request.getMethod())) {
            if (!isAllowed(loginAttemptCounts, ip + "_login", LOGIN_LIMIT, FIFTEEN_MINUTES)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many login attempts");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(Map<String, RequestInfo> cache, String key, int maxRequests, long timeWindowMillis) {
        long now = System.currentTimeMillis();
        RequestInfo info = cache.compute(key, (k, v) -> {
            if (v == null || now - v.startTime > timeWindowMillis) {
                return new RequestInfo(now, 1);
            }
            v.count++;
            return v;
        });
        return info.count <= maxRequests;
    }

    private static class RequestInfo {
        long startTime;
        int count;

        RequestInfo(long startTime, int count) {
            this.startTime = startTime;
            this.count = count;
        }
    }
}
