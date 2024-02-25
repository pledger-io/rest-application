package com.jongsoft.finance.rest.profile;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpHeaders;
import io.micronaut.test.annotation.MockBean;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Profile export resource")
class ProfileExportResourceTest extends TestSetup {

    @Replaces
    @MockBean
    TransactionProvider transactionProvider() {
        return Mockito.mock(TransactionProvider.class);
    }

    @Inject
    private TransactionProvider transactionProvider;

    @Test
    @DisplayName("should export profile")
    void export(RequestSpecification spec) {
        Mockito.when(transactionProvider.lookup(Mockito.any(TransactionProvider.FilterCommand.class)))
                        .thenReturn(ResultPage.of());

        // @formatter:off
        spec.when()
                .get("/api/profile/export")
            .then()
                .statusCode(200)
                .contentType("application/json")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test-user-profile.json\"");
        // @formatter:on
    }
}
