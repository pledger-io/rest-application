package com.jongsoft.finance.bpmn.delegate.rule;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.serialized.RuleConfigJson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ParseTransactionRuleDelegate implements JavaDelegate {

    private final StorageService storageService;

    @Inject
    public ParseTransactionRuleDelegate(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing raw json file in {}",
                execution.getCurrentActivityName(),
                execution.getActivityInstanceId());

        String storageToken = (String) execution.getVariableLocal("storageToken");
        final byte[] rawRuleConfig = storageService.read(storageToken);

        var configJson = RuleConfigJson.read(new String(rawRuleConfig));

        execution.setVariable("ruleLines", configJson.getRules());
    }

}
