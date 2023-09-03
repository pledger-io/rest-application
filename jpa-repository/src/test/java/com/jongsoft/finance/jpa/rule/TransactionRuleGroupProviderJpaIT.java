package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.TransactionRuleGroupProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

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
        StepVerifier.create(ruleGroupProvider.lookup())
                .consumeNextWith(rule -> Assertions.assertThat(rule.getName()).isEqualTo("Grocery stores"))
                .consumeNextWith(rule -> Assertions.assertThat(rule.getName()).isEqualTo("Hardware stores"))
                .verifyComplete();
    }

    @Test
    void lookup_name() {
        var check = ruleGroupProvider.lookup("Grocery stores");
        Assertions.assertThat(check.isPresent()).isTrue();
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
