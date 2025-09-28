package com.jongsoft.finance.bpmn.delegate;

import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.core.MailDaemon;
import jakarta.inject.Singleton;
import java.util.Properties;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

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
