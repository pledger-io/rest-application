package com.jongsoft.finance.rest.category;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MicronautTest
@DisplayName("Category Resource")
class CategoryResourceTest extends TestSetup {

    @Inject
    private CategoryProvider categoryProvider;

    @Replaces
    @MockBean
    CategoryProvider categoryProvider() {
        return Mockito.mock(CategoryProvider.class);
    }

    @Test
    @DisplayName("List all categories")
    void list(RequestSpecification spec) {
        when(categoryProvider.lookup()).thenReturn(Collections.List(
                Category.builder()
                        .id(1L)
                        .label("grocery")
                        .description("For groceries")
                        .lastActivity(LocalDate.of(2019, 1, 2))
                        .build()));

        // @formatter:off
        spec
            .when()
                .get("/api/categories")
            .then()
                .statusCode(200)
                .body("$", org.hamcrest.Matchers.hasSize(1))
                .body("[0].id", org.hamcrest.Matchers.equalTo(1));
        // @formatter:on
    }

    @Test
    @DisplayName("Search categories")
    void search(RequestSpecification spec) {
        when(categoryProvider.lookup(Mockito.any(CategoryProvider.FilterCommand.class))).thenReturn(ResultPage.of(
                Category.builder()
                        .id(1L)
                        .label("grocery")
                        .description("For groceries")
                        .lastActivity(LocalDate.of(2019, 1, 2))
                        .build()));

        // @formatter:off
        spec
            .given()
                .body(CategorySearchRequest.builder()
                        .page(1)
                        .build())
            .when()
                .post("/api/categories")
            .then()
                .statusCode(200)
                .body("info.records", org.hamcrest.Matchers.equalTo(1))
                .body("content", org.hamcrest.Matchers.hasSize(1))
                .body("content[0].id", org.hamcrest.Matchers.equalTo(1));
        // @formatter:on
    }

    @Test
    @DisplayName("Autocomplete categories by token")
    void autocomplete(RequestSpecification spec) {
        when(categoryProvider.lookup(Mockito.any(CategoryProvider.FilterCommand.class))).thenReturn(
                ResultPage.of(Category.builder()
                        .id(1L)
                        .label("grocery")
                        .user(ACTIVE_USER)
                        .description("For groceries")
                        .lastActivity(LocalDate.of(2019, 1, 2))
                        .build()));

        // @formatter:off
        spec
            .when()
                .get("/api/categories/auto-complete?token=gro")
            .then()
                .statusCode(200)
                .body("$", org.hamcrest.Matchers.hasSize(1))
                .body("[0].id", org.hamcrest.Matchers.equalTo(1));
        // @formatter:on

        var mockFilter = filterFactory.category();
        verify(categoryProvider).lookup(Mockito.any(CategoryProvider.FilterCommand.class));
        verify(mockFilter).label("gro", false);
    }

    @Test
    @DisplayName("Create category")
    void create(RequestSpecification spec) {
        when(categoryProvider.lookup("grocery")).thenReturn(
                Control.Option(Category.builder()
                        .id(1L)
                        .build()));

        // @formatter:off
        spec
            .given()
                .body(CategoryCreateRequest.builder()
                        .name("grocery")
                        .description("Sample")
                        .build())
            .when()
                .put("/api/categories")
            .then()
                .statusCode(201)
                .body("id", org.hamcrest.Matchers.equalTo(1));
        // @formatter:on
    }

    @Test
    @DisplayName("Fetch category")
    void get(RequestSpecification spec) {
        when(categoryProvider.lookup(1L)).thenReturn(
                Control.Option(Category.builder()
                        .id(1L)
                        .user(ACTIVE_USER)
                        .label("grocery")
                        .description("For groceries")
                        .lastActivity(LocalDate.of(2019, 1, 2))
                        .build()));

        // @formatter:off
        spec
            .when()
                .get("/api/categories/1")
            .then()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.equalTo(1))
                .body("label", org.hamcrest.Matchers.equalTo("grocery"))
                .body("description", org.hamcrest.Matchers.equalTo("For groceries"))
                .body("lastUsed", org.hamcrest.Matchers.equalTo("2019-01-02"));
        // @formatter:on
    }

    @Test
    @DisplayName("Fetch category not found")
    void get_notFound(RequestSpecification spec) {
        when(categoryProvider.lookup(1L)).thenReturn(Control.Option());

        // @formatter:off
        spec
            .when()
                .get("/api/categories/1")
            .then()
                .statusCode(404)
                .body("message", org.hamcrest.Matchers.equalTo("No category found with id 1"));
        // @formatter:on
    }

    @Test
    @DisplayName("Update category")
    void update(RequestSpecification spec) {
        Category category = Mockito.spy(Category.builder()
                .id(1L)
                .label("grocery")
                .user(ACTIVE_USER)
                .description("For groceries")
                .lastActivity(LocalDate.of(2019, 1, 2))
                .build());

        when(categoryProvider.lookup(1L)).thenReturn(Control.Option(category));

        // @formatter:off
        spec
            .given()
                .body(CategoryCreateRequest.builder()
                        .name("grocery")
                        .description("Sample")
                        .build())
            .when()
                .post("/api/categories/1")
            .then()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.equalTo(1))
                .body("label", org.hamcrest.Matchers.equalTo("grocery"))
                .body("description", org.hamcrest.Matchers.equalTo("Sample"))
                .body("lastUsed", org.hamcrest.Matchers.equalTo("2019-01-02"));
        // @formatter:on

        verify(category).rename("grocery", "Sample");
    }

    @Test
    @DisplayName("Update category not found")
    void update_notFound(RequestSpecification spec) {
        when(categoryProvider.lookup(1L)).thenReturn(Control.Option());

        // @formatter:off
        spec
            .given()
                .body(CategoryCreateRequest.builder()
                        .name("grocery")
                        .description("Sample")
                        .build())
            .when()
                .post("/api/categories/{id}", 1L)
            .then()
                .statusCode(404)
                .body("message", org.hamcrest.Matchers.equalTo("No category found with id 1"));
        // @formatter:on
    }

    @Test
    @DisplayName("Delete category")
    void delete(RequestSpecification spec) {
        Category category = Mockito.mock(Category.class);

        when(category.getUser()).thenReturn(ACTIVE_USER);
        when(categoryProvider.lookup(1L)).thenReturn(Control.Option(category));

        // @formatter:off
        spec
            .when()
                .delete("/api/categories/1")
            .then()
                .statusCode(204);
        // @formatter:on

        verify(category).remove();
    }
}
