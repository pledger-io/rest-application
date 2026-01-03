package com.jongsoft.finance.core.domain;

import com.jongsoft.finance.configuration.EventBus;

import java.io.Serializable;

public interface ApplicationEvent extends Serializable {

    default void publish() {
        EventBus.getBus().send(this);
    }
}
