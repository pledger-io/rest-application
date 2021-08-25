package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.importer.BatchImport;
import io.micronaut.core.annotation.Introspected;

import java.util.Date;

@Introspected
public class ImporterResponse {

    private final BatchImport wrapped;

    public ImporterResponse(BatchImport wrapped) {
        this.wrapped = wrapped;
    }

    public String getSlug() {
        return wrapped.getSlug();
    }

    public Date getCreated() {
        return wrapped.getCreated();
    }

    public Date getFinished() {
        return wrapped.getFinished();
    }

    public CSVImporterConfigResponse getConfig() {
        if (wrapped.getConfig() == null) {
            return null;
        }

        return new CSVImporterConfigResponse(wrapped.getConfig());
    }

    public Balance getBalance() {
        return new Balance();
    }

    public class Balance {

        public double getTotalIncome() {
            return wrapped.getTotalIncome();
        }

        public double getTotalExpense() {
            return wrapped.getTotalExpense();
        }

    }
}
