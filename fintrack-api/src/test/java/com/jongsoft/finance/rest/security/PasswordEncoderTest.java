package com.jongsoft.finance.rest.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.jongsoft.finance.security.PasswordEncoder;

class PasswordEncoderTest {

    @Test
    void matches() {
        var encoder = new PasswordEncoder();

        var encoded = encoder.encrypt("MySamplePasswordIsLong");

        Assertions.assertTrue(encoder.matches(encoded, "MySamplePasswordIsLong"));
    }
}
