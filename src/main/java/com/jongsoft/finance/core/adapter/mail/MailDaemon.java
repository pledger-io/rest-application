package com.jongsoft.finance.core.adapter.mail;

import java.util.Properties;

public interface MailDaemon {

    void send(String recipient, String template, Properties mailProperties);
}
