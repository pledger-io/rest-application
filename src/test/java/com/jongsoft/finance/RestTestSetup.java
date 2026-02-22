package com.jongsoft.finance;

import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.extension.PledgerTest;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;

@Tag("regression")
@MicronautTest(environments = {"jpa", "h2", "test", "test-jpa", "security"}, transactional = false)
@PledgerTest
public class RestTestSetup {

    @AfterEach
    void afterEach(PledgerContext pledgerContext) {
        pledgerContext.cleanStorage();
    }
}
