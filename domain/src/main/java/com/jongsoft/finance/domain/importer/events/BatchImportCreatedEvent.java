package com.jongsoft.finance.domain.importer.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.domain.user.UserAccount;

import lombok.Getter;

@Getter
public class BatchImportCreatedEvent implements ApplicationEvent {

    private BatchImportConfig config;
    private UserAccount user;
    private String slug;
    private String fileCode;

    public BatchImportCreatedEvent(Object source, BatchImportConfig config, UserAccount user, String slug, String fileCode) {
        this.config = config;
        this.user = user;
        this.slug = slug;
        this.fileCode = fileCode;
    }

}
