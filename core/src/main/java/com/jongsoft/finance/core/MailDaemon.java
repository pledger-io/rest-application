package com.jongsoft.finance.core;

import java.util.Properties;

public interface MailDaemon extends JavaBean {

  void send(String recipient, String template, Properties mailProperties);
}
