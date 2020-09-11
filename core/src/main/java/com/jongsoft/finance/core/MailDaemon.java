package com.jongsoft.finance.core;

import java.util.Properties;

public interface MailDaemon {

    void send(String recipient, String template, Properties mailProperties);

}
