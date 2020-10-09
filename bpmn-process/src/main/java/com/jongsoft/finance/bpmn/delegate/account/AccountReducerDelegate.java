package com.jongsoft.finance.bpmn.delegate.account;

import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Set;
import com.jongsoft.lang.collection.tuple.Pair;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.Comparator;
import java.util.List;

/**
 * The account reducer is a utility delegate used to generate a unique result of a list of {@link Pair} containing
 * the name used to match in the {@link Pair#getFirst()} and the account id linked in the {@link Pair#getSecond()}.
 *
 * This delegate expects the following variables:
 * <ul>
 *     <li>transactions, a map with key being potential account names and value being the account id</li>
 * </ul>
 *
 * The delegate will produce the following output:
 * <ul>
 *     <li>extractionResult, the list containing all pairs after processing</li>
 * </ul>
 */
@Slf4j
public class AccountReducerDelegate implements JavaDelegate {

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing reducing account extraction", execution.getCurrentActivityName());

        List<Pair<String, ?>> transactions = (List<Pair<String, ?>>) execution.getVariable("transactions");
        if (!transactions.isEmpty()) {
            Set<Pair<String, ?>> reducedTransactions = API.Set(
                    Comparator.comparing(Pair::getFirst),
                    transactions.get(0));

            for (int i=1; i < transactions.size(); i++) {
                reducedTransactions = reducedTransactions.append(transactions.get(i));
            }

            execution.setVariableLocal("extractionResult", reducedTransactions);
        } else {
            execution.setVariableLocal("extractionResult", API.List());
        }

    }

}
