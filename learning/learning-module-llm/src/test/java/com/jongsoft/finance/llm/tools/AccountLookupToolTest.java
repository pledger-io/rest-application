package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.llm.dto.AccountDTO;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.lang.Control;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountLookupToolTest {

    @Test
    void lookup() {
        var mockAccountProvider = mock(AccountProvider.class);
        var subject = new AccountLookupTool(mockAccountProvider);

        when(mockAccountProvider.lookup("My account"))
                .thenReturn(Control.Option(Account.builder().name("My account").type("checking").build()));

        var response = subject.lookup("My account");

        assertThat(response)
                .isNotNull()
                .isEqualTo(new AccountDTO("My account", "checking"));
    }

    @Test
    void lookupFallback() {
        var mockAccountProvider = mock(AccountProvider.class);
        var subject = new AccountLookupTool(mockAccountProvider);

        when(mockAccountProvider.synonymOf("My account"))
                .thenReturn(Control.Option(Account.builder().name("My account").type("checking").build()));

        var response = subject.lookup("My account");

        assertThat(response)
                .isNotNull()
                .isEqualTo(new AccountDTO("My account", "checking"));

        verify(mockAccountProvider).synonymOf("My account");
    }
}
