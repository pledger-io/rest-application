package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransactionTagResourceTest extends TestSetup {

    @Inject
    private TagProvider tagProvider;

    @Replaces
    @MockBean
    TagProvider mockTagProvider() {
        return Mockito.mock(TagProvider.class);
    }

    @Replaces
    @MockBean
    SettingProvider mockSettingProvider() {
        return Mockito.mock(SettingProvider.class);
    }

    @Test
    void create(RequestSpecification spec) {
        // @formatter:off
        spec
            .given()
                .body(new TagCreateRequest("Sample tag"))
            .when()
                .post("/api/transactions/tags")
            .then()
                .statusCode(200)
                .body("name", Matchers.equalTo("Sample tag"));
        // @formatter:on
    }

    @Test
    @DisplayName("List available tags")
    void list(RequestSpecification spec) {
        Mockito.when(tagProvider.lookup())
                .thenReturn(Collections.List(
                        new Tag("Sample"),
                        new Tag("Description")));

        // @formatter:off
        spec
            .when()
                .get("/api/transactions/tags")
            .then()
                .statusCode(200)
                .body("name", Matchers.hasItems("Sample", "Description"));
        // @formatter:on
    }

    @Test
    @DisplayName("should return a tag on autocomplete")
    void autoCompleteTag(RequestSpecification spec) {
        Mockito.when(tagProvider.lookup(Mockito.any(TagProvider.FilterCommand.class))).thenReturn(
                ResultPage.of(new Tag("Sample")));

        // @formatter:off
        spec
            .given()
                .queryParam("token", "samp")
            .when()
                .get("/api/transactions/tags/auto-complete")
            .then()
                .statusCode(200)
                .body("name", Matchers.hasItems("Sample"));
        // @formatter:on

        var mockFilter = filterFactory.tag();
        Mockito.verify(tagProvider).lookup(Mockito.any(TagProvider.FilterCommand.class));
        Mockito.verify(mockFilter).name("samp", false);
    }
}
