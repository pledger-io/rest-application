package com.jongsoft.finance.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import com.jongsoft.finance.core.Encoder;
import jakarta.inject.Singleton;
import java.security.SecureRandom;

@Singleton
public class PasswordEncoder implements Encoder {

  private static final int HASHING_STRENGTH = 10;

  private final BCrypt.Hasher hashApplier;

  public PasswordEncoder() {
    this.hashApplier = BCrypt.with(
        BCrypt.Version.VERSION_2A,
        new SecureRandom(),
        LongPasswordStrategies.hashSha512(BCrypt.Version.VERSION_2A));
  }

  public String encrypt(String password) {
    return hashApplier.hashToString(HASHING_STRENGTH, password.toCharArray());
  }

  public boolean matches(String hash, String password) {
    var result = BCrypt.verifyer().verify(password.toCharArray(), hash);

    return result.verified;
  }
}
