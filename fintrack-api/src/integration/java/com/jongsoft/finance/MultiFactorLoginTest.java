package com.jongsoft.finance;

import com.jongsoft.finance.extension.IntegrationTest;
import com.jongsoft.finance.extension.TestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

@IntegrationTest(phase = 1)
@DisplayName("User registers and logins with MFA")
public class MultiFactorLoginTest {

    @Test
    void registerAndLoginWithMFA(TestContext testContext) {
        var profileContext = testContext
                .register("mfa-sample@e", "Zomer2020")
                .authenticate("mfa-sample@e", "Zomer2020")
                .profile();

        profileContext
                .get(response -> response
                        .body("theme", equalTo("dark"))
                        .body("currency", equalTo("EUR"))
                        .body("mfa", equalTo(false)));

        testContext.enableMFA();

        testContext
                .authenticate("mfa-sample@e", "Zomer2020")
                .multiFactor()
                .profile()
                .get(response -> response
                        .body("theme", equalTo("dark"))
                        .body("currency", equalTo("EUR"))
                        .body("mfa", equalTo(true)));
    }
}
