package com.jongsoft.finance.exporter.domain.model;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.exporter.domain.commands.CompleteImportJobCommand;
import com.jongsoft.finance.exporter.domain.commands.CreateImportJobCommand;
import com.jongsoft.finance.exporter.domain.commands.DeleteImportJobCommand;

import io.micronaut.core.annotation.Introspected;

import java.util.Date;
import java.util.UUID;

@Introspected
public class BatchImport implements Classifier {

    private Long id;
    private Date created;
    private Date finished;

    private String slug;
    private String fileCode;

    private BatchImportConfig config;

    private BatchImport(BatchImportConfig config, String fileCode) {
        this.slug = UUID.randomUUID().toString();
        this.config = config;
        this.fileCode = fileCode;
        this.created = new Date();

        CreateImportJobCommand.importJobCreated(config.getId(), slug, fileCode);
    }

    BatchImport(
            Long id,
            Date created,
            Date finished,
            String slug,
            String fileCode,
            BatchImportConfig config) {
        this.id = id;
        this.created = created;
        this.finished = finished;
        this.slug = slug;
        this.fileCode = fileCode;
        this.config = config;
    }

    public void archive() {
        if (this.finished != null) {
            throw StatusException.badRequest(
                    "Cannot archive an import job that has finished running.");
        }

        DeleteImportJobCommand.importJobDeleted(id);
    }

    public void finish(Date date) {
        if (this.finished != null) {
            throw StatusException.badRequest(
                    "Cannot finish an import which has already completed.");
        }

        this.finished = date;
        CompleteImportJobCommand.importJobCompleted(id);
    }

    @Override
    public Long getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public Date getFinished() {
        return finished;
    }

    public String getSlug() {
        return slug;
    }

    public String getFileCode() {
        return fileCode;
    }

    public BatchImportConfig getConfig() {
        return config;
    }

    public static BatchImport create(BatchImportConfig config, String fileCode) {
        return new BatchImport(config, fileCode);
    }
}
