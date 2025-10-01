package com.jongsoft.finance.bpmn.delegate.user;

import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.providers.UserProvider;

import jakarta.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class UsernameAvailableDelegate implements JavaDelegate, JavaBean {

    private static final String USERNAME = "username";
    private static final Logger log = LoggerFactory.getLogger(UsernameAvailableDelegate.class);

    private final UserProvider userProvider;

    public UsernameAvailableDelegate(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug(
                "{}: Validating if the username is still available {}",
                execution.getCurrentActivityName(),
                execution.getVariableLocal(USERNAME));

        execution.setVariableLocal(
                "usernameAvailable",
                userProvider.available((UserIdentifier) execution.getVariableLocal(USERNAME)));
    }
}
