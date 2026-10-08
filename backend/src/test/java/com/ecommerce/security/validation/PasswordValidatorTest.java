package com.ecommerce.security.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordValidatorTest {

    private PasswordValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordValidator();
        validator.initialize(null);
    }

    @Test
    void testValidPasswords() {
        assertTrue(validator.isValid("StrongPass1!", null));
        assertTrue(validator.isValid("CorrectHorseBatteryStaple123#", null));
        assertTrue(validator.isValid("Aa1!@#$%^&+=_\\-.", null));
    }

    @Test
    void testInvalidPasswords() {
        assertFalse(validator.isValid(null, null)); // Null
        assertFalse(validator.isValid("", null)); // Empty
        assertFalse(validator.isValid("short1!", null)); // Too short (< 8)
        assertFalse(validator.isValid("nouppercase1!", null)); // No uppercase
        assertFalse(validator.isValid("NOLOWERCASE1!", null)); // No lowercase
        assertFalse(validator.isValid("NoDigitSpecial!", null)); // No digit
        assertFalse(validator.isValid("NoSpecialChar123", null)); // No special char
        assertFalse(validator.isValid("Spaces Not Allowed1!", null)); // Whitespace
    }
}
