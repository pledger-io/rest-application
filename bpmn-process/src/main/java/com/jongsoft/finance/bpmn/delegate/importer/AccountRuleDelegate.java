package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.rule.RuleDataSet;
import com.jongsoft.finance.rule.RuleEngine;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Slf4j
@Singleton
public class AccountRuleDelegate implements JavaDelegate {

    private final RuleEngine ruleEngine;

    @Inject
    public AccountRuleDelegate(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    public void execute(DelegateExecution execution) throws Exception {
        var name = (String) execution.getVariable("name");
        var description = (String) execution.getVariable("description");

        log.debug("{}: Locating account using rule system '{}' - {}",
                execution.getCurrentActivityName(),
                name,
                description);

        var inputSet = new RuleDataSet();
        inputSet.put(RuleColumn.TO_ACCOUNT, name);
        inputSet.put(RuleColumn.DESCRIPTION, description);

        var outputSet = ruleEngine.run(inputSet);
        if (outputSet.containsKey(RuleColumn.TO_ACCOUNT)) {
            var account = outputSet.<Account>getCasted(RuleColumn.TO_ACCOUNT);
            execution.setVariableLocal("accountId", account.getId());
        } else {
            execution.setVariableLocal("accountId", null);
        }
    }

}
