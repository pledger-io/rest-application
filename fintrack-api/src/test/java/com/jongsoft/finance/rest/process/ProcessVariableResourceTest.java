package com.jongsoft.finance.rest.process;

import org.camunda.bpm.engine.ProcessEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ProcessVariableResourceTest {

    private ProcessVariableResource subject;

    @Mock
    private ProcessEngine processEngine;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        subject = new ProcessVariableResource(processEngine);
    }

    @Test
    void variables() {
    }

    @Test
    void variable() {
    }

}