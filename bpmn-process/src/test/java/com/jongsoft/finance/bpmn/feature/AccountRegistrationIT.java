package com.jongsoft.finance.bpmn.feature;

import com.jongsoft.finance.bpmn.feature.junit.ProcessExtension;
import com.jongsoft.finance.bpmn.feature.junit.RuntimeContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@MicronautTest
@ProcessExtension
@DisplayName("Account registration feature")
public class AccountRegistrationIT {

    @Test
    @DisplayName("Register with already known username")
    void registerAlreadyKnown(RuntimeContext runtimeContext) {
        runtimeContext
                .execute(
                        "RegisterUserAccount",
                        Variables.createVariables()
                                .putValue("username", "test-user"))
                .verifyCompleted()
                .verifyErrorCompletion();
    }

    @Test
    @DisplayName("Register with new username")
    void registerNewUser(RuntimeContext runtimeContext) {
        runtimeContext
                .withoutUser()
                .execute(
                        "RegisterUserAccount",
                        Variables.createVariables()
                                .putValue("username", "new-user@local")
                                .putValue("passwordHash", "password-hash"))
                .verifyCompleted()
                .verifySuccess();

        runtimeContext.verifyMailSent("new-user@local", "user-registered", assertion ->
                assertion.hasSize(1)
                        .anySatisfy((key, value) -> {
                            Assertions.assertThat(key).isEqualTo("user");
                            Assertions.assertThat(value).isEqualTo("new-user@local");
                        }));
    }
}
