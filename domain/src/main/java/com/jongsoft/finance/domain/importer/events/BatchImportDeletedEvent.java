package com.jongsoft.finance.domain.importer.events;

import com.jongsoft.finance.core.ApplicationEvent;
import lombok.Getter;

@Getter
public class BatchImportDeletedEvent implements ApplicationEvent {

    private final long id;

    public BatchImportDeletedEvent(long id) {
        this.id = id;
    }

}
