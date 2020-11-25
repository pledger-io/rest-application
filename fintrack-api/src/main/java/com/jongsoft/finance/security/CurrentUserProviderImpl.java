package com.jongsoft.finance.security;

import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserProvider;
import com.jongsoft.lang.Control;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("currentUserProvider")
public class CurrentUserProviderImpl implements CurrentUserProvider {

    private final AuthenticationFacade authenticationFacade;
    private final UserProvider userProvider;

    public CurrentUserProviderImpl(final AuthenticationFacade AuthenticationFacade, final UserProvider userProvider) {
        this.authenticationFacade = AuthenticationFacade;
        this.userProvider = userProvider;
    }

    @Override
    public UserAccount currentUser() {
        var username = Control.Option(authenticationFacade.authenticated());
        return username.map(s ->
                userProvider.lookup(s)
                        .getOrSupply(() -> null))
                .getOrSupply(() -> null);

    }

}
