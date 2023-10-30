package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.importer.BatchImport;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Serdeable.Serializable
public class ImporterResponse {

    private final BatchImport wrapped;

    public ImporterResponse(BatchImport wrapped) {
        this.wrapped = wrapped;
    }

    @Schema(description = "The unique identifier of the import job", required = true, example = "83c3a405939f741cee534d48e600528c")
    public String getSlug() {
        return wrapped.getSlug();
    }

    @Schema(description = "The date the job was created", required = true, example = "2020-02-02T10:00:00.000Z")
    public Date getCreated() {
        return wrapped.getCreated();
    }

    @Schema(description = "The date the job was finished", example = "2020-03-02T12:00:00.000Z")
    public Date getFinished() {
        return wrapped.getFinished();
    }

    @Schema(description = "Get the configuration used during the import")
    public CSVImporterConfigResponse getConfig() {
        if (wrapped.getConfig() == null) {
            return null;
        }

        return new CSVImporterConfigResponse(wrapped.getConfig());
    }

    @Schema(description = "Get the affected balance during the import")
    public Balance getBalance() {
        return new Balance();
    }

    @Serdeable.Serializable
    public class Balance {

        @Schema(description = "The total amount of money earned in this import")
        public double getTotalIncome() {
            return wrapped.getTotalIncome();
        }

        @Schema(description = "The total amount of money spent in this import")
        public double getTotalExpense() {
            return wrapped.getTotalExpense();
        }

    }
}
