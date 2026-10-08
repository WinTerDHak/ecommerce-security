package com.ecommerce.security;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
public class HashTest {
    @Test
    public void checkHash() {
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16, 32, 1, 65536, 3);
        String hash = "$argon2id$v=19$m=65536,t=3,p=1$F15Detx6d9SypTnG5CZ2iw$77DyWtePvRSiuI8p/lZJavfEMYcknc57v3nflDErRgg";
        System.out.println("TEST_HASH_MATCH: " + encoder.matches("Admin@1234", hash));
    }
}
