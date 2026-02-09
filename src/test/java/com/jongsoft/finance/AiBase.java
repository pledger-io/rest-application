package com.jongsoft.finance;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

@MicronautTest(
        environments = {"jpa", "h2", "test", "test-jpa", "ai"},
        transactional = false)
public class AiBase extends RestTestSetup {}
