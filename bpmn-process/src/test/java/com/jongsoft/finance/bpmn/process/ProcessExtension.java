package com.jongsoft.finance.bpmn.process;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({
        ProcessTestExtension.class
})
public @interface ProcessExtension {
}
