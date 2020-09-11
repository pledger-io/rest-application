package com.jongsoft.finance.bpmn.delegate.rule;

import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.rule.RuleDataSet;
import com.jongsoft.finance.rule.RuleEngine;
import com.jongsoft.lang.collection.tuple.Triplet;

@Singleton
public class ExtractorRuleRunDelegate implements JavaDelegate {

    private final RuleEngine ruleEngine;

    public ExtractorRuleRunDelegate(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var nameIbanPair = (Triplet<String, String, String>) execution.getVariableLocal("accountLookup");

        var inputSet = new RuleDataSet();
        inputSet.put(RuleColumn.TO_ACCOUNT, nameIbanPair.getFirst());
        inputSet.put(RuleColumn.DESCRIPTION, nameIbanPair.getThird());

        var outputSet = ruleEngine.run(inputSet);
        if (outputSet.containsKey(RuleColumn.TO_ACCOUNT)) {
            var account = outputSet.<Account>getCasted(RuleColumn.TO_ACCOUNT);
            execution.setVariableLocal("id", account.getId());
        } else {
            execution.setVariableLocal("id", null);
        }
    }
}
