package com.jongsoft.finance.domain.importer.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class BatchImportFinishedEvent implements ApplicationEvent {

    private final Long importId;

    public BatchImportFinishedEvent(Object source, Long importId) {
        this.importId = importId;
    }

}
