package com.jongsoft.finance.bpmn.delegate.user;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.serialized.ExportJson;
import com.jongsoft.finance.serialized.RuleConfigJson;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class ParseUserConfigurationDelegate implements JavaDelegate {

    private final StorageService storageService;

    ParseUserConfigurationDelegate(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing raw json file in {}",
                execution.getCurrentActivityName(),
                execution.getActivityInstanceId());

        String storageToken = (String) execution.getVariable("storageToken");

        byte[] rawJsonContent = storageService.read(storageToken).block();
        ExportJson profileJson = ExportJson.read(new String(rawJsonContent, StandardCharsets.UTF_8));

        String ruleStorageToken = storageService.store(RuleConfigJson.builder()
                .slug("profile-import")
                .rules(profileJson.getRules())
                .build()
                .write().getBytes());

        execution.setVariableLocal("accounts", serialize(profileJson.getAccounts()));
        execution.setVariableLocal("budgetPeriods", serialize(profileJson.getBudgetPeriods()));
        execution.setVariableLocal("contracts", serialize(profileJson.getContracts()));
        execution.setVariableLocal("categories", serialize(profileJson.getCategories()));
        execution.setVariableLocal("tags", profileJson.getTags());
        execution.setVariableLocal("ruleStorageToken", ruleStorageToken);
    }

    List<String> serialize(List<?> input) {
        return input.stream()
                .map(ProcessMapper::writeSafe)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
