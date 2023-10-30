package com.jongsoft.finance.serialized;

import com.jongsoft.finance.ProcessMapper;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@Serdeable
public class ExportJson implements Serializable {

    private List<AccountJson> accounts;
    private List<RuleConfigJson.RuleJson> rules;
    private List<CategoryJson> categories;
    private List<BudgetJson> budgetPeriods;
    private List<ContractJson> contracts;
    private List<String> tags;

    public static ExportJson read(String value) {
        try {
            return ProcessMapper.INSTANCE.readValue(value, ExportJson.class);
        } catch (IOException e) {
            throw new IllegalStateException("Invalid configuration for configuration import provided.", e);
        }
    }
    
}
