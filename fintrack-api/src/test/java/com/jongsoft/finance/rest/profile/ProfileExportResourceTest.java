package com.jongsoft.finance.rest.profile;

import com.jongsoft.finance.rest.TestSetup;
import io.micronaut.http.HttpHeaders;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@MicronautTest
@DisplayName("Profile export resource")
class ProfileExportResourceTest extends TestSetup {

    @Test
    @DisplayName("should export profile")
    void export(RequestSpecification spec) {
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
