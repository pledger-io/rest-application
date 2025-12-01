package com.jongsoft.finance.rest;

import com.jongsoft.finance.rest.extension.PledgerContext;
import com.jongsoft.finance.rest.extension.PledgerRequests;
import com.jongsoft.finance.rest.extension.PledgerTest;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@MicronautTest(environments = {"jpa", "h2", "test"}, transactional = false)
@PledgerTest
public class TagTest {

    @Test
    void createNewTag(PledgerContext context, PledgerRequests requests) {
        context.withUser("tags-create@account.local");

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
