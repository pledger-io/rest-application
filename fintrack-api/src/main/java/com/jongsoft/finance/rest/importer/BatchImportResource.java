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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
    ImporterResponse create(@Valid @Body ImporterCreateRequest request) {
        return csvConfigProvider.lookup(request.getConfiguration())
                .map(config -> config.createImport(request.getUploadToken()))
                .map(ImporterResponse::new)
                .getOrThrow(() -> StatusException.notFound("CSV configuration not found"));
    }

    @Get("/{batchSlug}")
    @Operation(
            summary = "Get Importer Job",
            description = "Fetch a single importer job from FinTrack",
            parameters = @Parameter(name = "batchSlug", in = ParameterIn.PATH, description = "The unique identifier")
    )
    ImporterResponse get(@PathVariable String batchSlug) {
        return importProvider.lookup(batchSlug)
                .map(ImporterResponse::new)
                .getOrThrow(() -> StatusException.notFound("CSV configuration not found"));
    }

    @Delete("/{batchSlug}")
    @Operation(
            summary = "Delete importer job",
            description = "Removes an unfinished job from the system. Note that already completed jobs cannot be removed.",
            parameters = @Parameter(name = "batchSlug", in = ParameterIn.PATH, description = "The unique identifier")
    )
    @Status(value = HttpStatus.NO_CONTENT)
    String delete(@PathVariable String batchSlug) {
        return importProvider.lookup(batchSlug)
                .map(job -> {
                    job.archive();
                    return "";
                })
                .getOrThrow(() -> StatusException.notFound("Cannot delete import with slug " + batchSlug));
    }

    @Get("/config")
    @Operation(
            summary = "List configurations",
            description = "List all available importer configurations in FinTrack"
    )
    List<CSVImporterConfigResponse> config() {
        return csvConfigProvider.lookup()
                .map(CSVImporterConfigResponse::new)
                .toJava();
    }

    @Put("/config")
    @Operation(
            summary = "Create configuration",
            description = "Creates a new importer configuration in FinTrack, using the provided file token"
    )
    CSVImporterConfigResponse createConfig(@Valid @Body CSVImporterConfigCreateRequest request) {
        var existing = csvConfigProvider.lookup(request.getName());
        if (existing.isPresent()) {
            throw StatusException.badRequest("Configuration with name " + request.getName() + " already exists.");
        }

        return new CSVImporterConfigResponse(currentUserProvider.currentUser().createImportConfiguration(
                request.getName(),
                request.getFileCode()));
    }
}
