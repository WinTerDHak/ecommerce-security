package com.ecommerce.security;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtTest {
    @Test
    public void testJwt() {
        String jwtSecret = "TESTSECRETKEY12345678901234567890";
        try {
            Jwts.builder()
                .setSubject("test")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .compact();
            System.out.println("JWT_SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
