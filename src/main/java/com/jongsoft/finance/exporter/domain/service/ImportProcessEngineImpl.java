package com.jongsoft.finance.exporter.domain.service;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.core.adapter.api.StorageService;
import com.jongsoft.finance.core.domain.model.ProcessVariable;
import com.jongsoft.finance.exporter.adapter.api.ImportProcesEngine;
import com.jongsoft.finance.exporter.adapter.api.ImportProvider;
import com.jongsoft.finance.exporter.domain.model.BatchImport;
import com.jongsoft.finance.exporter.domain.model.UserTask;
import com.jongsoft.finance.exporter.domain.service.transaction.ImportProcess;
import com.jongsoft.lang.Control;

import jakarta.inject.Singleton;

import java.util.List;

@Singleton
class ImportProcessEngineImpl implements ImportProcesEngine {

    private final ImportProvider importProvider;
    private final StorageService storageService;
    private final List<ImporterProvider<?>> importerProviders;

    ImportProcessEngineImpl(
            ImportProvider importProvider,
            StorageService storageService,
            List<ImporterProvider<?>> importerProviders) {
        this.importProvider = importProvider;
        this.storageService = storageService;
        this.importerProviders = importerProviders;
    }

    @Override
    public List<UserTask> getTasksForBatch(String batchSlug) {
        BatchImport batchImport = importProvider
                .lookup(batchSlug)
                .getOrThrow(() ->
                        StatusException.notFound("Batch with slug " + batchSlug + " not found."));

        ImportProcess process = new ImportProcess(_ -> null, batchImport, storageService);
        Control.Try(() -> process.loadContext(importerProviders)).get();
        if (!process.isWaiting()) {
            throw StatusException.badRequest("Batch is not waiting for user tasks.");
        }

        return process.computeUserTasks();
    }

    @Override
    public void completeTask(String batchSlug, String taskId, ProcessVariable userData) {
        BatchImport batchImport = importProvider
                .lookup(batchSlug)
                .getOrThrow(() ->
                        StatusException.notFound("Batch with slug " + batchSlug + " not found."));

        ImportProcess process = new ImportProcess(_ -> null, batchImport, storageService);
        Control.Try(() -> process.loadContext(importerProviders)).get();
        if (!process.isWaiting()) {
            throw StatusException.badRequest("Batch is not waiting for user tasks.");
        }

        process.completeTask(taskId, userData);
        Control.Try(process::saveContext).get();
    }
}
