package com.jongsoft.finance.security;

import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserProvider;
import io.micronaut.security.utils.SecurityService;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("currentUserProvider")
public class CurrentUserProviderImpl implements CurrentUserProvider {

    private final SecurityService securityService;
    private final UserProvider userProvider;

    public CurrentUserProviderImpl(final SecurityService securityService, final UserProvider userProvider) {
        this.securityService = securityService;
        this.userProvider = userProvider;
    }

    @Override
    public UserAccount currentUser() {
        var username = securityService.username();
        return username.map(s ->
                userProvider.lookup(s)
                        .getOrSupply(() -> null))
                .orElse(null);

    }

}
