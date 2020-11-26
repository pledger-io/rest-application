package com.jongsoft.finance.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;

import javax.inject.Singleton;
import java.security.SecureRandom;

@Singleton
public class PasswordEncoder {

    private static final int HASHER_STRENGTH = 10;

    private final BCrypt.Hasher hasher;

    public PasswordEncoder() {
        this.hasher = BCrypt.with(
                BCrypt.Version.VERSION_2A,
                new SecureRandom(),
                LongPasswordStrategies.hashSha512(BCrypt.Version.VERSION_2A));
    }

    public String encrypt(String password) {
        return hasher.hashToString(HASHER_STRENGTH, password.toCharArray());
    }

    public boolean matches(String hash, String password) {
        var result = BCrypt.verifyer()
                .verify(password.toCharArray(), hash);

        return result.verified;
    }

}
