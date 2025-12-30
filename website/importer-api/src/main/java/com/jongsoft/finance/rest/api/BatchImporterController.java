package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.providers.ImportConfigurationProvider;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.rest.BatchImporterApi;
import com.jongsoft.finance.rest.model.*;
import com.jongsoft.finance.security.CurrentUserProvider;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Controller
class BatchImporterController implements BatchImporterApi {

    private final Logger logger;
    private final ImportProvider importProvider;
    private final ImportConfigurationProvider importConfigurationProvider;
    private final CurrentUserProvider currentUserProvider;

    BatchImporterController(
            ImportProvider importProvider,
            ImportConfigurationProvider importConfigurationProvider,
            CurrentUserProvider currentUserProvider) {
        this.importProvider = importProvider;
        this.importConfigurationProvider = importConfigurationProvider;
        this.currentUserProvider = currentUserProvider;
        this.logger = LoggerFactory.getLogger(BatchImporterController.class);
    }

    @Override
    public HttpResponse<@Valid ConfigurationResponse> createConfiguration(
            CreateConfigurationRequest createConfigurationRequest) {
        logger.info("Creating configuration.");
        importConfigurationProvider
                .lookup(createConfigurationRequest.getName())
                .ifPresent(() -> StatusException.badRequest("Configuration with name "
                        + createConfigurationRequest.getName() + " already exists."));

        var createdConfig = currentUserProvider
                .currentUser()
                .createImportConfiguration(
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

        var importJob = importConfig.createImport(createJobRequest.getFileToken());
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
    public List<@Valid ConfigurationResponse> getConfigurations() {
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
    public PagedBatchJobResponse getJobsByFilters(Integer numberOfResults, Integer offset) {
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

        return new PagedBatchJobResponse(
                new PagedResponseInfo(result.total(), result.pages(), result.pageSize()),
                result.content().map(this::convertToJobResponse).toJava());
    }

    private BatchJobResponse convertToJobResponse(BatchImport batchImport) {
        var response = new BatchJobResponse(
                batchImport.getSlug(),
                LocalDate.ofInstant(batchImport.getCreated().toInstant(), ZoneId.of("UTC")),
                convertToConfigResponse(batchImport.getConfig()),
                new BatchJobResponseBalance(
                        batchImport.getTotalIncome(), batchImport.getTotalExpense()));

        if (batchImport.getFinished() != null) {
            response.finished(
                    LocalDate.ofInstant(batchImport.getFinished().toInstant(), ZoneId.of("UTC")));
        }

        return response;
    }

    private ConfigurationResponse convertToConfigResponse(BatchImportConfig config) {
        return new ConfigurationResponse(
                config.getId(), config.getName(), config.getType(), config.getFileCode());
    }
}
