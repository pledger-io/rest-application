package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.TransactionRuleProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRuleProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private TransactionRuleProvider ruleProvider;

    @BeforeEach
    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/transaction/rule-group-provider.sql",
                "sql/transaction/rule-provider.sql"
        );
    }

    @Test
    void lookup() {
        var check = ruleProvider.lookup();
        assertThat(check).hasSize(1);
    }

    @Test
    void lookup_group() {
        StepVerifier.create(ruleProvider.lookup("Grocery stores"))
                .assertNext(rule -> assertThat(rule.getName()).isEqualTo("Income rule"))
                .verifyComplete();
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }

}
