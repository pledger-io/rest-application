package com.jongsoft.finance.serialized;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@Serdeable
public class ExportJson implements Serializable {

    private List<AccountJson> accounts;
    private List<RuleConfigJson.RuleJson> rules;
    private List<CategoryJson> categories;
    private List<BudgetJson> budgetPeriods;
    private List<ContractJson> contracts;
    private List<String> tags;

}
