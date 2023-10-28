package com.jongsoft.finance.rest.file;

import com.jongsoft.finance.StorageService;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

@MicronautTest
@DisplayName("Attachment Resource")
class FileResourceTest {

    @Inject
    private StorageService storageService;

    @Replaces
    @MockBean
    StorageService storageService() {
        return Mockito.mock(StorageService.class);
    }

    @Test
    @DisplayName("Upload file")
    void upload(RequestSpecification spec) throws IOException {
        Mockito.when(storageService.store("sample-data".getBytes())).thenReturn("sample-token");

        // @formatter:off
        spec
            .given()
                .multiPart("upload", new File(getClass().getResource("/application.yml").getFile()))
            .when()
                .post("/api/attachment")
            .then()
                .statusCode(201);
        // @formatter:on

        Mockito.verify(storageService).store(Mockito.any());
    }

    @Test
    @DisplayName("Download file")
    void download(RequestSpecification spec) {
        Mockito.when(storageService.read("fasjkdh8nfasd8")).thenReturn(Control.Option("sample-token".getBytes()));

        // @formatter:off
        spec.when()
                .get("/api/attachment/fasjkdh8nfasd8")
            .then()
                .statusCode(200)
                .body(Matchers.equalTo("sample-token"));
        // @formatter:on

        Mockito.verify(storageService).read("fasjkdh8nfasd8");
    }

    @Test
    @DisplayName("Delete file")
    void delete(RequestSpecification spec) {

        // @formatter:off
        spec.when()
                .delete("/api/attachment/fasjkdh8nfasd8")
            .then()
                .statusCode(204);
        // @formatter:on

        Mockito.verify(storageService).remove("fasjkdh8nfasd8");
    }
}
