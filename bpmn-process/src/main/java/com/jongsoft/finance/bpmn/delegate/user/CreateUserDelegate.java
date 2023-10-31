package com.jongsoft.finance.bpmn.delegate.user;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.serialized.AccountJson;
import jakarta.inject.Singleton;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

@Singleton
public class CreateUserDelegate implements JavaDelegate {

    private final FinTrack application;
    private final ProcessMapper mapper;

    CreateUserDelegate(FinTrack application, ProcessMapper mapper) {
        this.application = application;
        this.mapper = mapper;
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

        execution.setVariableLocal("account", mapper.writeSafe(reconcileAccount));
    }

}
