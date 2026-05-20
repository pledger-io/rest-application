package com.jongsoft.finance.core.adapter.rest;

import com.jongsoft.finance.RestTestSetup;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.core.value.UserIdentifier;
import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.extension.PledgerRequests;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Regression - File Upload")
class FileTest extends RestTestSetup {

    @Test
    @DisplayName("Upload a file and check it can be retrieved")
    void uploadAndCheckFile(PledgerContext context, PledgerRequests requests) throws IOException {
        context.withUser("file-upload-test@account.local").withStorage();
        requests.authenticate("file-upload-test@account.local");

        String fileCode =
                requests.createAttachment("/logback.xml")
                        .statusCode(201)
                        .body("fileCode", notNullValue())
                        .extract()
                        .body()
                        .jsonPath()
                        .getString("fileCode");

        byte[] fileContent =
                requests.fetchAttachment(fileCode).statusCode(200)
                    .extract().body().asByteArray();

        var expected = getClass().getResourceAsStream("/logback.xml").readAllBytes();
        assertThat(fileContent).containsExactly(expected);

        requests.deleteAttachment(fileCode)
                .statusCode(204);

        requests.fetchAttachment(fileCode)
                .statusCode(404);
    }
}
