package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.banking.types.FailureCode;

public record RegisterFailureCommand(long id, FailureCode code) implements ApplicationEvent {

    public static void registerFailure(long id, FailureCode code) {
        new RegisterFailureCommand(id, code).publish();
    }
}
