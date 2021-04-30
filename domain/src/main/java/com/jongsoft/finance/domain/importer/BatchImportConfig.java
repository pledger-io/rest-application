package com.jongsoft.finance.domain.importer;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.importer.CreateConfigurationCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@Aggregate
@AllArgsConstructor
public class BatchImportConfig implements AggregateBase, Serializable {

    private Long id;

    private String name;
    private String fileCode;

    private transient UserAccount user;

    @BusinessMethod
    public BatchImportConfig(UserAccount user, String name, String fileCode) {
        this.user = user;
        this.name = name;
        this.fileCode = fileCode;

        EventBus.getBus().send(
                new CreateConfigurationCommand(name, fileCode));
    }

    public BatchImport createImport(String content) {
        return new BatchImport(this, this.user, content);
    }

}
