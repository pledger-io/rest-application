package com.jongsoft.finance.rest.importer;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.domain.importer.CSVConfigProvider;
import com.jongsoft.finance.domain.importer.ImportProvider;
import com.jongsoft.finance.rest.model.CSVImporterConfigResponse;
import com.jongsoft.finance.rest.model.ImporterResponse;
import com.jongsoft.finance.rest.model.ResultPageResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "Importer")
@Controller("/api/import")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class BatchImportResource {

    private final CurrentUserProvider currentUserProvider;
    private final CSVConfigProvider csvConfigProvider;
    private final ImportProvider importProvider;
    private final SettingProvider settingProvider;

    public BatchImportResource(
            CurrentUserProvider currentUserProvider,
            CSVConfigProvider csvConfigProvider,
            ImportProvider importProvider,
            SettingProvider settingProvider) {
        this.currentUserProvider = currentUserProvider;
        this.csvConfigProvider = csvConfigProvider;
        this.importProvider = importProvider;
        this.settingProvider = settingProvider;
    }

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
    Single<ImporterResponse> create(@Valid @Body ImporterCreateRequest request) {
        return Single.create(emitter -> {
            csvConfigProvider.lookup(request.getConfiguration())
                    .map(config -> config.createImport(request.getUploadToken()))
                    .map(ImporterResponse::new)
                    .ifPresent(emitter::onSuccess)
                    .elseRun(() -> emitter.onError(StatusException.notFound("CSV configuration not found")));
        });
    }

    @Get("/{batchSlug}")
    @Operation(
            summary = "Get Importer Job",
            description = "Fetch a single importer job from FinTrack",
            parameters = @Parameter(name = "batchSlug", in = ParameterIn.PATH, description = "The unique identifier")
    )
    Single<ImporterResponse> get(@PathVariable String batchSlug) {
        return importProvider.lookup(batchSlug)
                .map(ImporterResponse::new)
                .switchIfEmpty(Single.error(StatusException.notFound("CSV configuration not found")));
    }

    @Get("/config")
    @Operation(
            summary = "List configurations",
            description = "List all available importer configurations in FinTrack"
    )
    List<BatchImportConfig> config() {
        return csvConfigProvider.lookup().toJava();
    }

    @Put("/config")
    @Operation(
            summary = "Create configuration",
            description = "Creates a new importer configuration in FinTrack, using the provided file token"
    )
    Single<CSVImporterConfigResponse> createConfig(@Valid @Body CSVImporterConfigCreateRequest request) {
        return Single.create(emitter -> {
            csvConfigProvider.lookup(request.getName())
                    .ifPresent(x -> emitter.onError(StatusException.badRequest(
                            "Configuration with name " + request.getName() + " already exists.")))
                    .elseRun(() -> {
                        var config = currentUserProvider.currentUser().createImportConfiguration(
                                request.getName(),
                                request.getFileCode());
                        emitter.onSuccess(new CSVImporterConfigResponse(config));
                    });
        });
    }
}
