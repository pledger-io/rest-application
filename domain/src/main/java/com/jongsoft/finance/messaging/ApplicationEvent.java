package com.jongsoft.finance.messaging;

import java.io.Serializable;

public interface ApplicationEvent extends Serializable {

  default void publish() {
    EventBus.getBus().send(this);
  }
}
