package com.jongsoft.finance.rest.importer;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.providers.CSVConfigProvider;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
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

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

@DisplayName("Batch importing resource")
class BatchImportResourceTest extends TestSetup {

    @Inject
    private ImportProvider importProvider;
    @Inject
    private CSVConfigProvider csvConfigProvider;

    @Replaces
    @MockBean
    CSVConfigProvider configProvider() {
        return Mockito.mock(CSVConfigProvider.class);
    }

    @Replaces
    @MockBean
    ImportProvider importProvider() {
        return Mockito.mock(ImportProvider.class);
    }

    @Test
    @DisplayName("List all existing import jobs")
    void list(RequestSpecification spec) {
        var resultPage = ResultPage.of(BatchImport.builder()
                .id(1L)
                .created(DateUtils.toDate(LocalDate.of(2019, 1, 1)))
                .slug("batch-import-slug")
                .fileCode("sample big content")
                .build());
        Mockito.when(importProvider.lookup(Mockito.any(ImportProvider.FilterCommand.class))).thenReturn(resultPage);

        // @formatter:off
        spec
            .given()
                .body(new ImportSearchRequest(0))
            .when()
                .post("/api/import")
            .then()
                .statusCode(200)
                .body("info.records", Matchers.equalTo(1))
                .body("content[0].slug", Matchers.equalTo("batch-import-slug"));
        // @formatter:on
    }

    @Test
    @DisplayName("Create a new import job")
    void create(RequestSpecification spec) {
        var mockConfig = Mockito.mock(BatchImportConfig.class);

        Mockito.when(csvConfigProvider.lookup("sample-configuration")).thenReturn(Control.Option(mockConfig));
        Mockito.when(mockConfig.createImport("token-sample")).thenReturn(
                BatchImport.builder()
                        .created(DateUtils.toDate(LocalDate.of(2019, 2, 1)))
                        .fileCode("token-sample")
                        .slug("xd2rsd-2fasd-q2ff-asd")
                        .build());

        // @formatter:off
        spec
            .given()
                .body(Map.of("configuration", "sample-configuration", "uploadToken", "token-sample"))
            .when()
                .put("/api/import")
            .then()
                .statusCode(200)
                .body("slug", Matchers.equalTo("xd2rsd-2fasd-q2ff-asd"));
        // @formatter:on

        Mockito.verify(mockConfig).createImport("token-sample");
    }

    @Test
    @DisplayName("Get an existing import job")
    void get(RequestSpecification spec) {
        Mockito.when(importProvider.lookup("xd2rsd-2fasd-q2ff-asd")).thenReturn(
                Control.Option(BatchImport.builder()
                        .created(DateUtils.toDate(LocalDate.of(2019, 2, 1)))
                        .fileCode("token-sample")
                        .slug("xd2rsd-2fasd-q2ff-asd")
                        .config(BatchImportConfig.builder()
                                .id(1L)
                                .fileCode("xd2rsd-2fasd-33dfd-ddfa")
                                .name("sample-config.json")
                                .build())
                        .finished(DateUtils.toDate(LocalDate.of(2019, 2, 2)))
                        .totalExpense(200.2D)
                        .totalIncome(303.40D)
                        .build()));

        // @formatter:off
        spec
            .when()
                .get("/api/import/xd2rsd-2fasd-q2ff-asd")
            .then()
                .statusCode(200)
                .body("slug", Matchers.equalTo("xd2rsd-2fasd-q2ff-asd"))
                .body("config.file", Matchers.equalTo("xd2rsd-2fasd-33dfd-ddfa"))
                .body("config.name", Matchers.equalTo("sample-config.json"))
                .body("balance.totalExpense", Matchers.equalTo(200.2F))
                .body("balance.totalIncome", Matchers.equalTo(303.4F));
        // @formatter:on
    }

    @Test
    @DisplayName("Delete an existing import job")
    void delete_success(RequestSpecification spec) {
        Mockito.when(importProvider.lookup("xd2rsd-2fasd-q2ff-asd")).thenReturn(
                Control.Option(BatchImport.builder()
                        .id(1L)
                        .created(DateUtils.toDate(LocalDate.of(2019, 2, 1)))
                        .fileCode("token-sample")
                        .slug("xd2rsd-2fasd-q2ff-asd")
                        .config(BatchImportConfig.builder()
                                .id(1L)
                                .fileCode("xd2rsd-2fasd-33dfd-ddfa")
                                .name("sample-config.json")
                                .build())
                        .totalExpense(200.2D)
                        .totalIncome(303.40D)
                        .build()));

        // @formatter:off
        spec
            .when()
                .delete("/api/import/xd2rsd-2fasd-q2ff-asd")
            .then()
                .statusCode(204);
        // @formatter:on
    }

    @Test
    @DisplayName("Delete an existing import job that has already finished")
    void delete_alreadyFinished(RequestSpecification spec) {
        Mockito.when(importProvider.lookup("xd2rsd-2fasd-q2ff-asd")).thenReturn(
                Control.Option(BatchImport.builder()
                        .created(DateUtils.toDate(LocalDate.of(2019, 2, 1)))
                        .fileCode("token-sample")
                        .slug("xd2rsd-2fasd-q2ff-asd")
                        .config(BatchImportConfig.builder()
                                .id(1L)
                                .fileCode("xd2rsd-2fasd-33dfd-ddfa")
                                .name("sample-config.json")
                                .build())
                        .finished(new Date())
                        .totalExpense(200.2D)
                        .totalIncome(303.40D)
                        .build()));

        // @formatter:off
        spec
            .when()
                .delete("/api/import/xd2rsd-2fasd-q2ff-asd")
            .then()
                .statusCode(400)
                .body("message", Matchers.equalTo("Cannot archive an import job that has finished running."));
        // @formatter:on
    }

    @Test
    @DisplayName("List all existing import configurations")
    void config(RequestSpecification spec) {
        Mockito.when(csvConfigProvider.lookup()).thenReturn(Collections.List(
                BatchImportConfig.builder()
                        .id(1L)
                        .name("Import config test")
                        .build()));

        // @formatter:off
        spec
            .when()
                .get("/api/import/config")
            .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(1))
                .body("[0].name", Matchers.equalTo("Import config test"));
        // @formatter:on
    }

    @Test
    @DisplayName("Create a new import configuration")
    void createConfig(RequestSpecification spec) {
        Mockito.when(csvConfigProvider.lookup("sample-configuration")).thenReturn(Control.Option());

        // @formatter:off
        spec
            .given()
                .body(Map.of("configuration", "sample-configuration", "uploadToken", "token-sample"))
            .when()
                .put("/api/import/config")
            .then()
                .statusCode(200)
                .body("name", Matchers.equalTo("sample-configuration"))
                .body("file", Matchers.equalTo("token-sample"));
        // @formatter:on
    }
}
