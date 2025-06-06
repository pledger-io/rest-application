package com.jongsoft.finance.security;

public interface AuthenticationFacade {

  /**
   * Get the authenticated username.
   *
   * @return the authenticated username
   */
  String authenticated();
}
