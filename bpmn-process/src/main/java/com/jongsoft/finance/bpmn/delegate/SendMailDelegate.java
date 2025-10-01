package com.jongsoft.finance.bpmn.delegate;

import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.core.MailDaemon;

import jakarta.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.Properties;

@Singleton
public class SendMailDelegate implements JavaDelegate, JavaBean {

    private final MailDaemon mailDaemon;

    public SendMailDelegate(MailDaemon mailDaemon) {
        this.mailDaemon = mailDaemon;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var email = (String) execution.getVariableLocal("email");
        var template = (String) execution.getVariableLocal("mailTemplate");
        var variables = (Properties) execution.getVariableLocal("variables");

        mailDaemon.send(email, template, variables);
    }
}
