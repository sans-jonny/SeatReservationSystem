package org.cleancoders.userandauth.outbound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutboundSignaturesTest {

    @Test
    void userRepositoryShouldDefineExpectedMethods() {
        var methods = UserRepository.class.getDeclaredMethods();
        assertEquals(3, methods.length,
            "UserRepository should define findByUsername, findById, and save");
    }

    @Test
    void passwordEncoderShouldDefineExpectedMethods() {
        var methods = PasswordEncoder.class.getDeclaredMethods();
        assertEquals(2, methods.length,
            "PasswordEncoder should define encode and matches");
    }

    @Test
    void tokenServiceShouldDefineGenerateMethod() {
        var methods = TokenService.class.getDeclaredMethods();
        assertEquals(1, methods.length,
            "TokenService should define generate");
    }
}
