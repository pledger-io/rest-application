package com.jongsoft.finance.security;

import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.lang.Control;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("currentUserProvider")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CurrentUserProviderImpl implements CurrentUserProvider {

    private final AuthenticationFacade authenticationFacade;
    private final UserProvider userProvider;

    @Override
    public UserAccount currentUser() {
        var username = Control.Option(authenticationFacade.authenticated());
        return username.map(s ->
                userProvider.lookup(s)
                        .getOrSupply(() -> null))
                .getOrSupply(() -> null);

    }

}
