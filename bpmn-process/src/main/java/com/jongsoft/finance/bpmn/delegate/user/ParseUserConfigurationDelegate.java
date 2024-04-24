package com.jongsoft.finance.bpmn.delegate.user;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.serialized.BudgetJson;
import com.jongsoft.finance.serialized.ExportJson;
import com.jongsoft.finance.serialized.RuleConfigJson;
import com.jongsoft.lang.Control;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class ParseUserConfigurationDelegate implements JavaDelegate, JavaBean {

    private final StorageService storageService;
    private final ProcessMapper mapper;

    ParseUserConfigurationDelegate(StorageService storageService, ProcessMapper mapper) {
        this.storageService = storageService;
        this.mapper = mapper;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing raw json file in {}",
                execution.getCurrentActivityName(),
                execution.getActivityInstanceId());

        String storageToken = (String) execution.getVariable("storageToken");

        var profileJson = storageService.read(storageToken)
                .map(String::new)
                .map(json -> mapper.readSafe(json, ExportJson.class))
                .getOrThrow(() -> new RuntimeException("Unable to parse json file"));

        if (profileJson.getRules() != null && !profileJson.getRules().isEmpty()) {
            String ruleStorageToken = storageService.store(
                    mapper.writeSafe(RuleConfigJson.builder()
                            .slug("profile-import")
                            .rules(profileJson.getRules())
                            .build()).getBytes(StandardCharsets.UTF_8));
            execution.setVariableLocal("ruleStorageToken", ruleStorageToken);
        } else {
            execution.setVariableLocal("ruleStorageToken", null);
        }

        if (profileJson.getBudgetPeriods() != null) {
            var sortedBudgets = profileJson.getBudgetPeriods().stream()
                    .sorted(Comparator.comparing(BudgetJson::getStart))
                    .toList();
            execution.setVariableLocal("budgetPeriods", serialize(sortedBudgets));
        } else {
            execution.setVariableLocal("budgetPeriods", List.of());
        }

        execution.setVariableLocal("transactions", Control.Option(profileJson.getTransactions())
                    .getOrSupply(List::of));

        execution.setVariableLocal("accounts", serialize(profileJson.getAccounts()));
        execution.setVariableLocal("contracts", serialize(profileJson.getContracts()));
        execution.setVariableLocal("categories", serialize(profileJson.getCategories()));
        execution.setVariableLocal("tags", Control.Option(profileJson.getTags()).getOrSupply(List::of));
    }


    List<String> serialize(List<?> input) {
        if (input == null) {
            return List.of();
        }

        return input.stream()
                .map(mapper::writeSafe)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
