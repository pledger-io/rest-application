package com.jongsoft.finance.bpmn.delegate.rule;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.serialized.RuleConfigJson;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Slf4j
@Singleton
public class ParseTransactionRuleDelegate implements JavaDelegate, JavaBean {

    private final StorageService storageService;
    private final ProcessMapper mapper;

    ParseTransactionRuleDelegate(StorageService storageService, ProcessMapper mapper) {
        this.storageService = storageService;
        this.mapper = mapper;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug(
                "{}: Processing raw json file in {}",
                execution.getCurrentActivityName(),
                execution.getActivityInstanceId());

        String storageToken = (String) execution.getVariableLocal("storageToken");

        var rules = storageService
                .read(storageToken)
                .map(String::new)
                .map(json -> mapper.readSafe(json, RuleConfigJson.class))
                .map(RuleConfigJson::getRules)
                .getOrThrow(() -> new RuntimeException("Failed to read json file"));

        execution.setVariable("ruleLines", rules);
    }
}
