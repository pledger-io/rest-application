package com.jongsoft.finance.core.domain.service;

import com.jongsoft.finance.core.adapter.api.CurrentUserProvider;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.core.value.UserIdentifier;
import com.jongsoft.lang.Control;

import jakarta.inject.Singleton;

@Singleton
class CurrentUserProviderImpl implements CurrentUserProvider {

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
