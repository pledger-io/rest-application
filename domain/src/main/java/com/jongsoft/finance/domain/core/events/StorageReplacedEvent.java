package com.jongsoft.finance.domain.core.events;

import com.jongsoft.finance.core.ApplicationEvent;
import lombok.Getter;

@Getter
public class StorageReplacedEvent implements ApplicationEvent {

    private final String fileCode;
    private final String oldFileCode;

    public StorageReplacedEvent(String fileCode, String oldFileCode) {
        this.fileCode = fileCode;
        this.oldFileCode = oldFileCode;
    }

}
