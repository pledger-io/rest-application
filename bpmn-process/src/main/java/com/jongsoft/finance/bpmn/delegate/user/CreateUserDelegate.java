package com.jongsoft.finance.bpmn.delegate.user;

import com.jongsoft.finance.domain.FinTrack;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

public class CreateUserDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var toCreateUsername = execution.<StringValue>getVariableLocalTyped("username").getValue();
        var toCreatePassword = execution.<StringValue>getVariableLocalTyped("password").getValue();;

        FinTrack.createUser(toCreateUsername, toCreatePassword);
    }

}
