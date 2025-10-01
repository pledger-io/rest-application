package com.jongsoft.finance.security;

import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.lang.Control;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Named("currentUserProvider")
public class CurrentUserProviderImpl implements CurrentUserProvider {

    private final AuthenticationFacade authenticationFacade;
    private final UserProvider userProvider;

    public CurrentUserProviderImpl(
            AuthenticationFacade authenticationFacade, UserProvider userProvider) {
        this.authenticationFacade = authenticationFacade;
        this.userProvider = userProvider;
    }

    @Override
    public UserAccount currentUser() {
        var username = Control.Option(authenticationFacade.authenticated());
        return username.map(s -> userProvider.lookup(new UserIdentifier(s)).getOrSupply(() -> null))
                .getOrSupply(() -> null);
    }
}
