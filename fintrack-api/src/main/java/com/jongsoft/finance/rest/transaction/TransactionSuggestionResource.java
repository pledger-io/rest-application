package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.rest.model.AccountResponse;
import com.jongsoft.finance.rest.model.CategoryResponse;
import com.jongsoft.finance.rest.model.ExpenseResponse;
import com.jongsoft.finance.rest.model.TagResponse;
import com.jongsoft.finance.rule.RuleDataSet;
import com.jongsoft.finance.rule.RuleEngine;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.lang.Control;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Transactions")
@Controller("/api/transactions/suggestions")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
class TransactionSuggestionResource {

    private final RuleEngine ruleEngine;

    TransactionSuggestionResource(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    @Post
    @Operation(
            operationId = "suggestTransaction",
            summary = "Suggest changes",
            description = "Suggest changes to a transaction based upon the rules in the system.")
    Map<String, ?> suggest(@Body TransactionForSuggestionRequest request) {
        var ruleDataset = new RuleDataSet();
        Control.Option(request.amount()).ifPresent(value -> ruleDataset.put(RuleColumn.AMOUNT, value));
        Control.Option(request.source()).ifPresent(value -> ruleDataset.put(RuleColumn.SOURCE_ACCOUNT, value));
        Control.Option(request.destination()).ifPresent(value -> ruleDataset.put(RuleColumn.TO_ACCOUNT, value));
        Control.Option(request.description()).ifPresent(value -> ruleDataset.put(RuleColumn.DESCRIPTION, value));

        var suggestions = new HashMap<String, Object>();
        for (var suggestion : ruleEngine.run(ruleDataset).entrySet()) {
            var outputSuggestion = switch (suggestion.getValue()) {
                case Account account -> new AccountResponse(account);
                case Category category -> new CategoryResponse(category);
                case Budget.Expense expense -> new ExpenseResponse(expense);
                case com.jongsoft.finance.domain.transaction.Tag tag -> new TagResponse(tag);

                default -> throw StatusException.internalError("Could not convert suggestion correctly for type " + suggestion.getValue());
            };

            suggestions.put(suggestion.getKey().toString(), outputSuggestion);
        }
        return suggestions;
    }
}
