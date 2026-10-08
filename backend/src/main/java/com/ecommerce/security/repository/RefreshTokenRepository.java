package com.ecommerce.security.repository;
import com.ecommerce.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ecommerce.security.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    @Modifying
    int deleteByUser(User user);}

