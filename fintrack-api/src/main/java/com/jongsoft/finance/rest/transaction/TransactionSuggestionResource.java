package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.learning.SuggestionEngine;
import com.jongsoft.finance.learning.SuggestionInput;
import com.jongsoft.finance.security.AuthenticationRoles;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Transactions")
@Controller("/api/transactions/suggestions")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
class TransactionSuggestionResource {

    private final SuggestionEngine suggestionEngine;

    TransactionSuggestionResource(SuggestionEngine suggestionEngine) {
        this.suggestionEngine = suggestionEngine;
    }

    @Post
    @Operation(
            operationId = "suggestTransaction",
            summary = "Suggest changes",
            description = "Suggest changes to a transaction based upon the rules in the system.")
    Map<String, ?> suggest(@Body TransactionForSuggestionRequest request) {

        var transactionInput = new SuggestionInput(
                LocalDate.now(),
                request.description(),
                request.source(),
                request.destination(),
                Optional.ofNullable(request.amount()).orElse(0D));
        var suggestions = suggestionEngine.makeSuggestions(transactionInput);

        var output = new HashMap<String, Object>();

        if (suggestions.budget() != null) {
            output.put(RuleColumn.BUDGET.toString(), suggestions.budget());
        }
        if (suggestions.tags() != null) {
            output.put(RuleColumn.TAGS.toString(), suggestions.tags().stream().map(TagCreateRequest::new).toList());
        }
        if (suggestions.category() != null) {
            output.put(RuleColumn.CATEGORY.toString(), suggestions.category());
        }

        return output;
    }
}
