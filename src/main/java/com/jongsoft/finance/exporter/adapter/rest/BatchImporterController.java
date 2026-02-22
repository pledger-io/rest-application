package com.jongsoft.finance.exporter.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.exporter.adapter.api.ImportConfigurationProvider;
import com.jongsoft.finance.exporter.adapter.api.ImportProcesEngine;
import com.jongsoft.finance.exporter.adapter.api.ImportProvider;
import com.jongsoft.finance.exporter.domain.model.BatchImport;
import com.jongsoft.finance.exporter.domain.model.BatchImportConfig;
import com.jongsoft.finance.exporter.domain.model.UserTask;
import com.jongsoft.finance.rest.BatchImporterApi;
import com.jongsoft.finance.rest.BatchTaskEngineApi;
import com.jongsoft.finance.rest.model.*;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Controller
class BatchImporterController implements BatchImporterApi, BatchTaskEngineApi {

    private final Logger logger;
    private final ImportProvider importProvider;
    private final ImportConfigurationProvider importConfigurationProvider;
    private final ImportProcesEngine importProcessEngine;

    BatchImporterController(
            ImportProvider importProvider,
            ImportConfigurationProvider importConfigurationProvider,
            ImportProcesEngine importProcessEngine) {
        this.importProvider = importProvider;
        this.importConfigurationProvider = importConfigurationProvider;
        this.importProcessEngine = importProcessEngine;
        this.logger = LoggerFactory.getLogger(BatchImporterController.class);
    }

    @Override
    public HttpResponse<@Valid BatchConfigurationResponse> createConfiguration(
            CreateConfigurationRequest createConfigurationRequest) {
        logger.info("Creating configuration.");
        importConfigurationProvider
                .lookup(createConfigurationRequest.getName())
                .ifPresent(() -> StatusException.badRequest("Configuration with name "
                        + createConfigurationRequest.getName() + " already exists."));

        var createdConfig = BatchImportConfig.create(
                createConfigurationRequest.getType(),
                createConfigurationRequest.getName(),
                createConfigurationRequest.getFileCode());
        return HttpResponse.created(convertToConfigResponse(createdConfig));
    }

    @Override
    public HttpResponse<@Valid BatchJobResponse> createJob(CreateJobRequest createJobRequest) {
        logger.info(
                "Creating new import job with config {}.", createJobRequest.get_configuration());
        var importConfig = importConfigurationProvider
                .lookup(createJobRequest.get_configuration())
                .getOrThrow(() -> StatusException.badRequest(
                        "Configuration " + createJobRequest.get_configuration() + " not found."));

        var importJob = BatchImport.create(importConfig, createJobRequest.getFileToken());
        return HttpResponse.created(convertToJobResponse(importJob));
    }

    @Override
    public HttpResponse<Void> deleteJobBySlug(String slug) {
        logger.info("Deleting job with slug {}.", slug);
        importProvider
                .lookup(slug)
                .getOrThrow(() -> StatusException.notFound("Job with slug " + slug + " not found."))
                .archive();
        return HttpResponse.noContent();
    }

    @Override
    public List<@Valid BatchConfigurationResponse> getConfigurations() {
        logger.info("Retrieving all configurations from the system.");
        return importConfigurationProvider
                .lookup()
                .map(this::convertToConfigResponse)
                .toJava();
    }

    @Override
    public BatchJobResponse getJobBySlug(String slug) {
        logger.info("Retrieving job with slug {}.", slug);
        var importJob = importProvider
                .lookup(slug)
                .getOrThrow(
                        () -> StatusException.notFound("Job with slug " + slug + " not found."));
        return convertToJobResponse(importJob);
    }

    @Override
    public BatchJobPagedResponse getJobsByFilters(Integer numberOfResults, Integer offset) {
        logger.info("Retrieving jobs by filters.");
        var result = importProvider.lookup(new ImportProvider.FilterCommand() {
            @Override
            public int page() {
                return offset / numberOfResults;
            }

            @Override
            public int pageSize() {
                return numberOfResults;
            }
        });

        return new BatchJobPagedResponse(
                new PagedResponseInfo(result.total(), result.pages(), result.pageSize()),
                result.content().map(this::convertToJobResponse).toJava());
    }

    @Override
    public List<@Valid TaskResponse> getTasks(String slug) {
        logger.info("Retrieving tasks for job with slug {}.", slug);
        List<UserTask> tasksForBatch = importProcessEngine.getTasksForBatch(slug);
        return tasksForBatch.stream()
                .map(task -> {
                    TaskResponse converted = new TaskResponse(task.id(), task.type());
                    converted.setVariables(task.properties());
                    return converted;
                })
                .toList();
    }

    @Override
    public HttpResponse<Void> completeTask(String slug, CompleteTaskRequest request) {
        logger.info("Completing task '{}' for {}.", request.getName(), slug);
        importProcessEngine.completeTask(slug, request.getName(), request.getVariables());
        return HttpResponse.noContent();
    }

    private BatchJobResponse convertToJobResponse(BatchImport batchImport) {
        var response = new BatchJobResponse(
                batchImport.getSlug(),
                LocalDate.ofInstant(batchImport.getCreated().toInstant(), ZoneId.of("UTC")),
                convertToConfigResponse(batchImport.getConfig()),
                new BatchJobResponseBalance(0D, 0D));

        if (batchImport.getFinished() != null) {
            response.finished(
                    LocalDate.ofInstant(batchImport.getFinished().toInstant(), ZoneId.of("UTC")));
        }

        return response;
    }

    private BatchConfigurationResponse convertToConfigResponse(BatchImportConfig config) {
        return new BatchConfigurationResponse(
                config.getId(), config.getName(), config.getType(), config.getFileCode());
    }
}
