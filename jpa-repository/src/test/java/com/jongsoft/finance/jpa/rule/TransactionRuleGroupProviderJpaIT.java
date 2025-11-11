package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.TransactionRuleGroupProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransactionRuleGroupProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private TransactionRuleGroupProvider ruleGroupProvider;

    @BeforeEach
    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/transaction/rule-group-provider.sql"
        );
    }

    @Test
    void lookup() {
        Assertions.assertThat(ruleGroupProvider.lookup())
                .hasSize(2)
                .first()
                .satisfies(rule -> Assertions.assertThat(rule.getName()).isEqualTo("Grocery stores"));
    }

    @Test
    void lookup_name() {
        var check = ruleGroupProvider.lookup("Grocery stores");
        Assertions.assertThat(check.isPresent()).isTrue();
    }

    @MockBean
    @Replaces
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
