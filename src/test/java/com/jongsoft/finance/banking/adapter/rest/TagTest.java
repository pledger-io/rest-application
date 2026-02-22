package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.RestTestSetup;
import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.extension.PledgerRequests;
import org.junit.jupiter.api.DisplayName;import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@DisplayName("Regression - Tags")
public class TagTest  extends RestTestSetup {

    @Test
    @DisplayName("Create, update, fetch and delete a tag")
    void createNewTag(PledgerContext context, PledgerRequests requests) {
        context.withUser("tags-create@account.local");
        requests.authenticate("tags-create@account.local");

        requests.createTag("Holidays 2023")
              .statusCode(204);

        requests.createTag("Vacation 2023")
              .statusCode(204);

        requests.createTag("Vacation 2024")
              .statusCode(204);

        requests.createTag("Vacation 2024")
              .statusCode(400)
              .body("message", equalTo("Tag with name Vacation 2024 already exists"));

        requests.searchTags("vaca")
              .statusCode(200)
              .body("$", hasSize(2))
              .body("[0]", equalTo("Vacation 2023"))
              .body("[1]", equalTo("Vacation 2024"));

        requests.deleteTag("Vacation 2023")
              .statusCode(204);

        requests.searchTags("vaca")
              .statusCode(200)
              .body("$", hasSize(1))
              .body("[0]", equalTo("Vacation 2024"));
    }

}
