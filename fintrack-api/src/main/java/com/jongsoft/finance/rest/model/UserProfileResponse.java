package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.lang.API;

import java.util.Currency;

public class UserProfileResponse {

    private final transient UserAccount wrappedModel;

    public UserProfileResponse(final UserAccount wrappedModel) {
        this.wrappedModel = wrappedModel;
    }

    public String getTheme() {
        return wrappedModel.getTheme();
    }

    public String getCurrency() {
        return API.Option(wrappedModel.getPrimaryCurrency())
                .map(Currency::getCurrencyCode)
                .getOrSupply(() -> null);
    }

    public String getProfilePicture() {
        return wrappedModel.getProfilePicture();
    }

    public boolean isMfa() {
        return wrappedModel.isTwoFactorEnabled();
    }

}
