package com.jongsoft.finance.rest;

import com.jongsoft.finance.rest.extension.PledgerContext;
import com.jongsoft.finance.rest.extension.PledgerRequests;
import com.jongsoft.finance.rest.extension.PledgerTest;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.jongsoft.lang.Dates.range;
import static java.time.LocalDate.of;
import static org.hamcrest.Matchers.notNullValue;

@MicronautTest(environments = {"jpa", "h2", "test"}, transactional = false)
@PledgerTest
public class StatisticalTest {

    @Test
    void computeBalanceForAccount(PledgerContext context, PledgerRequests requests) {
        context.withUser("compute-balance-account@account.local")
            .withBankAccount("Checking", "EUR", "default")
            .withCreditor("Netflix", "EUR")
            .withCategory("Streaming video")
            .withCategory("TV Services")
            .withTag("TV")
            .withTag("Streaming")
            .withTransaction("Checking", "Netflix", 25.22)
                .withTags("TV", "Streaming")
                .withCategory("Streaming video")
                .on(of(2023, 1, 1))
                .upsert()
            .withTransaction("Checking", "Netflix", 25.22)
                .withTags("Streaming")
                .withCategory("TV Services")
                .on(of(2023, 2, 1))
                .upsert();

        requests.computeBalance(range(of(2023, 1, 1), of(2023, 3, 1)), List.of(), List.of())
            .statusCode(200)
            .body("balance", Matchers.equalTo(-50.44F));

        var catId = requests.searchCategories(0, 2, "stream")
            .body("content[0].id", notNullValue())
            .extract().jsonPath().getLong("content[0].id");

        requests.computeBalance(range(of(2023, 1, 1), of(2023, 3, 1)), List.of(), List.of(catId))
            .statusCode(200)
            .body("balance", Matchers.equalTo(-25.22F));
    }
}
