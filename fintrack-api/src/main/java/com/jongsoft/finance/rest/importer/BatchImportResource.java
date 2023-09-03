package com.jongsoft.finance.rest.importer;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.providers.CSVConfigProvider;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.rest.model.CSVImporterConfigResponse;
import com.jongsoft.finance.rest.model.ImporterResponse;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@Tag(name = "Importer")
@Controller("/api/import")
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BatchImportResource {

    private final CurrentUserProvider currentUserProvider;
    private final CSVConfigProvider csvConfigProvider;
    private final ImportProvider importProvider;
    private final SettingProvider settingProvider;

    @Post
    @Operation(
            summary = "List jobs",
            description = "This operation will list all the run importer jobs for the current user"
    )
    ResultPageResponse<ImporterResponse> list(@Valid @Body ImportSearchRequest request) {
        var result = importProvider.lookup(new ImportProvider.FilterCommand() {
            @Override
            public int page() {
                return request.getPage();
            }

            @Override
            public int pageSize() {
                return settingProvider.getPageSize();
            }
        });

        return new ResultPageResponse<>(result.map(ImporterResponse::new));
    }

    @Put
    @Operation(
            summary = "Create importer",
            description = "Creates a new importer job in FinTrack, which can be used to import a CSV of transactions"
    )
    Publisher<ImporterResponse> create(@Valid @Body ImporterCreateRequest request) {
        return Mono.create(emitter -> {
            csvConfigProvider.lookup(request.getConfiguration())
                    .map(config -> config.createImport(request.getUploadToken()))
                    .map(ImporterResponse::new)
                    .ifPresent(e -> emitter.success(e))
                    .elseRun(() -> emitter.error(StatusException.notFound("CSV configuration not found")));
        });
    }

    @Get("/{batchSlug}")
    @Operation(
            summary = "Get Importer Job",
            description = "Fetch a single importer job from FinTrack",
            parameters = @Parameter(name = "batchSlug", in = ParameterIn.PATH, description = "The unique identifier")
    )
    Publisher<ImporterResponse> get(@PathVariable String batchSlug) {
        return importProvider.lookup(batchSlug)
                .map(ImporterResponse::new)
                .switchIfEmpty(Mono.error(StatusException.notFound("CSV configuration not found")));
    }

    @Delete("/{batchSlug}")
    @Operation(
            summary = "Delete importer job",
            description = "Removes an unfinished job from the system. Note that already completed jobs cannot be removed.",
            parameters = @Parameter(name = "batchSlug", in = ParameterIn.PATH, description = "The unique identifier")
    )
    @Status(value = HttpStatus.NO_CONTENT)
    Publisher<String> delete(@PathVariable String batchSlug) {
        return importProvider.lookup(batchSlug)
                .switchIfEmpty(Mono.error(StatusException.notFound("Cannot delete import with slug " + batchSlug)))
                .map(job -> {
                    job.archive();
                    return "";
                });
    }

    @Get("/config")
    @Operation(
            summary = "List configurations",
            description = "List all available importer configurations in FinTrack"
    )
    Publisher<CSVImporterConfigResponse> config() {
        return csvConfigProvider.lookup()
                .map(CSVImporterConfigResponse::new);
    }

    @Put("/config")
    @Operation(
            summary = "Create configuration",
            description = "Creates a new importer configuration in FinTrack, using the provided file token"
    )
    Publisher<CSVImporterConfigResponse> createConfig(@Valid @Body CSVImporterConfigCreateRequest request) {
        return Mono.create(emitter -> {
            csvConfigProvider.lookup(request.getName())
                    .ifPresent(x -> emitter.error(StatusException.badRequest(
                            "Configuration with name " + request.getName() + " already exists.")))
                    .elseRun(() -> {
                        var config = currentUserProvider.currentUser().createImportConfiguration(
                                request.getName(),
                                request.getFileCode());
                        emitter.success(new CSVImporterConfigResponse(config));
                    });
        });
    }
}
