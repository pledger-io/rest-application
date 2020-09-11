package com.jongsoft.finance.serialized;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.jongsoft.finance.ProcessMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
