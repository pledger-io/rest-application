package com.jongsoft.finance.suggestion.domain.service.ai;

import com.jongsoft.finance.RestTestSetup;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

@MicronautTest(
        environments = {"jpa", "h2", "test", "test-jpa", "ai"},
        transactional = false)
class AiBase extends RestTestSetup {}
