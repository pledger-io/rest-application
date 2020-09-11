package com.jongsoft.finance.rest;

import java.util.Currency;

import org.jboss.aerogear.security.otp.api.Base32;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.lang.API;

public class TestSetup {

    protected final UserAccount ACTIVE_USER = UserAccount.builder()
            .id(1L)
            .username("test-user")
            .password("1234")
            .theme("dark")
            .primaryCurrency(Currency.getInstance("EUR"))
            .secret(Base32.random())
            .roles(API.List(new Role("admin")))
            .build();

}
