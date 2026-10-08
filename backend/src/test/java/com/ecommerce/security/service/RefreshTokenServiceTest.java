package com.ecommerce.security.service;

import com.ecommerce.security.entity.RefreshToken;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.exception.TokenRefreshException;
import com.ecommerce.security.repository.RefreshTokenRepository;
import com.ecommerce.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private RefreshToken token;
    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 604800000L);

        user = new User();
        user.setId(1L);
        user.setEmail("test@ecommerce.local");

        token = new RefreshToken();
        token.setId(1L);
        token.setUser(user);
        token.setToken("random-uuid-token");
    }

    @Test
    void testVerifyExpiration_ValidToken_Succeeds() {
        token.setExpiresAt(LocalDateTime.now().plusDays(1));
        token.setRevoked(false);

        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertNotNull(result);
        assertEquals(token.getToken(), result.getToken());
    }

    @Test
    void testVerifyExpiration_ExpiredToken_Rejected() {
        token.setExpiresAt(LocalDateTime.now().minusDays(1));
        token.setRevoked(false);

        TokenRefreshException exception = assertThrows(TokenRefreshException.class, () -> {
            refreshTokenService.verifyExpiration(token);
        });

        assertTrue(exception.getMessage().contains("Refresh token was expired"));
        verify(refreshTokenRepository, times(1)).delete(token);
    }

    @Test
    void testVerifyExpiration_RevokedToken_Rejected() {
        token.setExpiresAt(LocalDateTime.now().plusDays(1));
        token.setRevoked(true);

        TokenRefreshException exception = assertThrows(TokenRefreshException.class, () -> {
            refreshTokenService.verifyExpiration(token);
        });

        assertTrue(exception.getMessage().contains("Refresh token was revoked"));
        verify(refreshTokenRepository, never()).delete(token);
    }
}
