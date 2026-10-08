package com.ecommerce.security;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
public class HashGen {
    public static void main(String[] args) {
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16, 32, 1, 65536, 3);
        System.out.println("HASH=" + encoder.encode("YOUR_PASSWORD_HERE"));
    }
}

