package com.jongsoft.finance.classification.adapter.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.jongsoft.finance.RestTestSetup;
import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.extension.PledgerRequests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Regression - Categories")
public class CategoryTest extends RestTestSetup {

    @Test
    @DisplayName("Create, update, fetch and delete a category")
    void createNewCategory(PledgerContext context, PledgerRequests requests) {
        context.withUser("category-create@account.local");
        requests.authenticate("category-create@account.local");

        var id = requests.createCategory("Groceries", "Grocery items")
              .statusCode(201)
              .body("name", equalTo("Groceries"))
              .body("description", equalTo("Grocery items"))
              .body("id", notNullValue())
              .extract().jsonPath().getLong("id");

        requests.fetchCategory(id)
              .statusCode(200)
              .body("name", equalTo("Groceries"))
              .body("description", equalTo("Grocery items"))
              .body("id", equalTo((int) id));

        requests.updateCategory(id, "Groceries", "Real items")
              .statusCode(200)
              .body("name", equalTo("Groceries"))
              .body("description", equalTo("Real items"))
              .body("id", equalTo((int) id));

        requests.deleteCategory(id)
              .statusCode(204);

        requests.fetchCategory(id)
              .statusCode(410)
              .body("message", equalTo("Category has been removed from the system"));
    }

    @Test
    @DisplayName("Search categories")
    void searchCategories(PledgerContext context, PledgerRequests requests) {
        context.withUser("category-search@account.local")
              .withCategory("Grocery")
              .withCategory("Shopping")
              .withCategory("Transportation");
        requests.authenticate("category-search@account.local");

        requests.searchCategories(0, 3, null)
              .statusCode(200)
              .body("info.records", equalTo(3))
              .body("info.pages", equalTo(1))
              .body("info.pageSize", equalTo(3))
              .body("content[0].name", equalTo("Grocery"))
              .body("content[1].name", equalTo("Shopping"))
              .body("content[2].name", equalTo("Transportation"));

        requests.searchCategories(0, 3, "groc")
              .statusCode(200)
              .body("info.records", equalTo(1))
              .body("info.pages", equalTo(1))
              .body("info.pageSize", equalTo(3))
              .body("content[0].name", equalTo("Grocery"));

        requests.searchCategories(0, 3, "Groc")
            .statusCode(200)
            .body("info.records", equalTo(1))
            .body("info.pages", equalTo(1))
            .body("info.pageSize", equalTo(3))
            .body("content[0].name", equalTo("Grocery"));
    }
}
