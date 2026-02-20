package com.jongsoft.finance;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.extension.PledgerTest;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.mockito.Mockito;

@Tag("regression")
@MicronautTest(environments = {"jpa", "h2", "test", "test-jpa"}, transactional = false)
@PledgerTest
public class RestTestSetup {

    @MockBean
    @Replaces
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }

    @AfterEach
    void afterEach(PledgerContext pledgerContext) {
        pledgerContext.cleanStorage();
    }
}
