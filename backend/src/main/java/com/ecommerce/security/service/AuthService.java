package com.ecommerce.security.service;

import com.ecommerce.security.dto.request.LoginRequest;
import com.ecommerce.security.dto.request.RegisterRequest;
import com.ecommerce.security.dto.response.JwtResponse;
import com.ecommerce.security.dto.response.UserResponse;
import com.ecommerce.security.entity.RefreshToken;
import com.ecommerce.security.entity.Role;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.repository.UserRepository;
import com.ecommerce.security.security.CustomUserDetails;
import com.ecommerce.security.security.jwt.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService auditLogService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
                       PasswordEncoder passwordEncoder, JwtUtils jwtUtils,
                       RefreshTokenService refreshTokenService, AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public UserResponse registerUser(RegisterRequest request, String ipAddress) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setActive(true);

        User savedUser = userRepository.save(user);
        
        auditLogService.logEvent(savedUser.getId(), "USER_REGISTERED", "User", savedUser.getId(), "New user registered", ipAddress);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getRole().name()
        );
    }

    public JwtResponse authenticateUser(LoginRequest request, String ipAddress) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        
        auditLogService.logEvent(userDetails.getId(), "LOGIN_SUCCESS", "User", userDetails.getId(), "User logged in", ipAddress);

        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        // Optional: you can extract firstName/lastName if added to CustomUserDetails, 
        // or fetch from DB. For simplicity we look it up.
        User user = userRepository.findById(userDetails.getId()).orElseThrow();

        return new JwtResponse(
                jwt,
                refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                role
        );
    }
    
    @Transactional
    public void logout(Long userId, String ipAddress) {
        refreshTokenService.deleteByUserId(userId);
        auditLogService.logEvent(userId, "LOGOUT", "User", userId, "User logged out", ipAddress);
    }
}
