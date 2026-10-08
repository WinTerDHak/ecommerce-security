package com.ecommerce.security.controller;

import com.ecommerce.security.dto.request.LoginRequest;
import com.ecommerce.security.dto.request.RegisterRequest;
import com.ecommerce.security.dto.request.TokenRefreshRequest;
import com.ecommerce.security.dto.response.JwtResponse;
import com.ecommerce.security.dto.response.MessageResponse;
import com.ecommerce.security.dto.response.TokenRefreshResponse;
import com.ecommerce.security.dto.response.UserResponse;
import com.ecommerce.security.entity.RefreshToken;
import com.ecommerce.security.exception.TokenRefreshException;
import com.ecommerce.security.security.CustomUserDetails;
import com.ecommerce.security.security.jwt.JwtUtils;
import com.ecommerce.security.security.jwt.JwtBlacklistService;
import com.ecommerce.security.service.AuthService;
import com.ecommerce.security.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;
    private final JwtBlacklistService jwtBlacklistService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService, JwtUtils jwtUtils, JwtBlacklistService jwtBlacklistService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtils = jwtUtils;
        this.jwtBlacklistService = jwtBlacklistService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
        try {
            UserResponse response = authService.registerUser(registerRequest, request.getRemoteAddr());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            JwtResponse response = authService.authenticateUser(loginRequest, request.getRemoteAddr());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Invalid credentials"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateJwtTokenFromUsername(user.getEmail());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtBlacklistService.blacklistToken(authHeader.substring(7));
        }
        try {
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId = userDetails.getId();
            authService.logout(userId, request.getRemoteAddr());
            return ResponseEntity.ok(new MessageResponse("Log out successful!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("User is not logged in"));
        }
    }
}
