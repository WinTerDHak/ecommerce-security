package com.ecommerce.security.security.jwt;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtBlacklistService {

    // Simple in-memory blacklist to avoid requiring Redis infrastructure
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public void blacklistToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        blacklistedTokens.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /** For test teardown only — clears the in-memory blacklist. */
    public void clear() {
        blacklistedTokens.clear();
    }
}
