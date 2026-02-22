package com.jongsoft.finance;

import java.io.Serializable;

public interface ApplicationEvent extends Serializable {

    default void publish() {
        EventBus.getBus().send(this);
    }
}
