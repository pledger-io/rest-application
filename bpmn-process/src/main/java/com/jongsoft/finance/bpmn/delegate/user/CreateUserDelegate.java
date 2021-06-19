package com.jongsoft.finance.bpmn.delegate.user;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.serialized.AccountJson;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

import javax.inject.Singleton;

@Singleton
public class CreateUserDelegate implements JavaDelegate {

    private final FinTrack application;

    public CreateUserDelegate(FinTrack application) {
        this.application = application;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var toCreateUsername = execution.<StringValue>getVariableLocalTyped("username").getValue();
        var toCreatePassword = execution.<StringValue>getVariableLocalTyped("password").getValue();;

        application.createUser(toCreateUsername, toCreatePassword);

        var reconcileAccount = AccountJson.builder()
                .currency("EUR")
                .description("Reconcile Account")
                .name("Reconcile Account")
                .type(SystemAccountTypes.RECONCILE.label())
                .build();

        execution.setVariableLocal("account", ProcessMapper.writeSafe(reconcileAccount));
    }

}
