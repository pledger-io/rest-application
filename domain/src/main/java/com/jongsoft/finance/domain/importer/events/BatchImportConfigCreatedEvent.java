package com.jongsoft.finance.domain.importer.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.user.UserAccount;

import lombok.Getter;

@Getter
public class BatchImportConfigCreatedEvent implements ApplicationEvent {

    private final UserAccount user;
    private final String name;
    private final String fileCode;

    public BatchImportConfigCreatedEvent(Object source, UserAccount user, String name, String fileCode) {
        this.user = user;
        this.name = name;
        this.fileCode = fileCode;
    }

}
