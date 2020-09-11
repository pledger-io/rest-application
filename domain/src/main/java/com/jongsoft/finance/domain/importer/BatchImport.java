package com.jongsoft.finance.domain.importer;

import java.util.Date;
import java.util.UUID;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.domain.importer.events.BatchImportCreatedEvent;
import com.jongsoft.finance.domain.importer.events.BatchImportFinishedEvent;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.EventBus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@Aggregate
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class BatchImport implements AggregateBase {

    private Long id;
    private Date created;
    private Date finished;

    private String slug;
    private String fileCode;

    private transient BatchImportConfig config;
    private transient UserAccount user;

    private double totalIncome;
    private double totalExpense;

    @BusinessMethod
    public BatchImport(BatchImportConfig config, UserAccount user, String fileCode) {
        this.user = user;
        this.slug = UUID.randomUUID().toString();
        this.config = config;
        this.fileCode = fileCode;

        EventBus.getBus().send(new BatchImportCreatedEvent(this, config, user, slug, fileCode));
    }

    @BusinessMethod
    public void finish(Date date) {
        if (this.finished != null) {
            throw new IllegalStateException("Cannot finish an import which has already completed.");
        }

        this.finished = date;
        EventBus.getBus().send(new BatchImportFinishedEvent(this, id));
    }

}
