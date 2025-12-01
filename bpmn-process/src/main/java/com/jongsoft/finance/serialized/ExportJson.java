package com.jongsoft.finance.serialized;

import io.micronaut.jsonschema.JsonSchema;
import io.micronaut.serde.annotation.Serdeable;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@Serdeable
@JsonSchema(title = "Profile", description = "A user profile", uri = "/profile")
public class ExportJson implements Serializable {

    private List<AccountJson> accounts;
    private List<RuleConfigJson.RuleJson> rules;
    private List<CategoryJson> categories;
    private List<BudgetJson> budgetPeriods;
    private List<ContractJson> contracts;
    private List<String> tags;
    private List<TransactionJson> transactions;
}
