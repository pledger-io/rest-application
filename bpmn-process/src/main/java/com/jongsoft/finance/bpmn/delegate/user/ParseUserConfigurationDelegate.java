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

        String ruleStorageToken = storageService.store(
                mapper.writeSafe(RuleConfigJson.builder()
                        .slug("profile-import")
                        .rules(profileJson.getRules())
                        .build()).getBytes(StandardCharsets.UTF_8));

        execution.setVariableLocal("accounts", serialize(profileJson.getAccounts()));
        execution.setVariableLocal("budgetPeriods", serialize(profileJson.getBudgetPeriods()));
        execution.setVariableLocal("contracts", serialize(profileJson.getContracts()));
        execution.setVariableLocal("categories", serialize(profileJson.getCategories()));
        execution.setVariableLocal("tags", profileJson.getTags());
        execution.setVariableLocal("ruleStorageToken", ruleStorageToken);
    }

    List<String> serialize(List<?> input) {
        return input.stream()
                .map(mapper::writeSafe)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
