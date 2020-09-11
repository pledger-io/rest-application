package com.jongsoft.finance.jpa.transaction;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.transaction.TransactionRuleGroupProvider;
import com.jongsoft.finance.jpa.JpaTestSetup;

import io.micronaut.test.annotation.MockBean;

class TransactionRuleGroupProviderJpaTest extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private TransactionRuleGroupProvider ruleGroupProvider;

    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/base-setup.sql",
                "sql/transaction/rule-group-provider.sql"
        );
    }

    @Test
    void lookup() {
        setup();
        var check = ruleGroupProvider.lookup();
        Assertions.assertThat(check).hasSize(2);
    }

    @Test
    void lookup_name() {
        setup();
        var check = ruleGroupProvider.lookup("Grocery stores");
        Assertions.assertThat(check.isPresent()).isTrue();
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
