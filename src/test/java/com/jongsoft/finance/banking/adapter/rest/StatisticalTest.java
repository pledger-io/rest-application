package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.RestTestSetup;
import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.extension.PledgerRequests;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.jongsoft.lang.Dates.range;
import static java.time.LocalDate.of;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("Regression - Statistical")
public class StatisticalTest extends RestTestSetup {

    @Test
    @DisplayName("Compute balance for an account")
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
        requests.authenticate("compute-balance-account@account.local");

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

    @Test
    @DisplayName("Compute balance split by account")
    void computeBalanceSplitByAccount(PledgerContext context, PledgerRequests requests) {
        context.withUser("compute-balance-split-account@account.local")
            .withBankAccount("Checking", "EUR", "default")
            .withCreditor("Netflix", "EUR")
            .withCreditor("Wallmart", "EUR")
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
        requests.authenticate("compute-balance-split-account@account.local");

        Long netflixId = requests.searchBankAccounts(0, 1, List.of("creditor"), "Netflix")
            .statusCode(200)
            .extract().jsonPath().getLong("content[0].id");

        requests.computeBalanceSplitAccount(range(of(2023, 1, 1), of(2023, 3, 1)), List.of(), List.of())
            .statusCode(200)
            .body("[0].balance", Matchers.equalTo(50.44F))
            .body("[0].partition", Matchers.equalTo("Netflix"));

        requests.computeBalanceSplitAccount(range(of(2023, 1, 1), of(2023, 3, 1)), List.of(netflixId), List.of())
            .statusCode(200)
            .body("[0].balance", Matchers.equalTo(50.44F))
            .body("[0].partition", Matchers.equalTo("Netflix"));
    }
}
