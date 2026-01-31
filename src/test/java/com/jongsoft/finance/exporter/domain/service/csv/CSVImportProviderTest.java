package com.jongsoft.finance.exporter.domain.service.csv;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.banking.types.TransactionType;
import com.jongsoft.finance.core.adapter.api.StorageService;
import com.jongsoft.finance.exporter.domain.model.BatchImport;
import com.jongsoft.finance.exporter.domain.model.BatchImportConfig;
import com.jongsoft.finance.exporter.domain.service.TransactionConsumer;
import com.jongsoft.finance.exporter.domain.service.TransactionDTO;
import com.jongsoft.lang.Control;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.annotation.MockBean;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

class CSVImportProviderTest extends JpaTestSetup {

    @Inject
    private CSVImportProvider csvImportProvider;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private StorageService storageService;

    @MockBean
    @Replaces
    StorageService storageService() {
        return Mockito.mock(StorageService.class);
    }

    @BeforeEach
    void setup() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql");
    }

    @Test
    @DisplayName("Load configuration file is missing")
    void loadConfiguration_fileMissing() {
        Assertions.assertThatException()
                .describedAs("No CSV configuration found on disk: my-secret-files")
                .isThrownBy(() -> csvImportProvider.loadConfiguration(createBatchImportConfig()))
                .isInstanceOf(IllegalStateException.class)
                .withMessage("No CSV configuration found on disk: my-secret-files");
    }

    @Test
    @DisplayName("Load configuration file that is valid")
    void loadConfiguration() throws IOException {
        Mockito.when(storageService.read("my-secret-files"))
                .thenReturn(Control.Option(getClass()
                        .getResourceAsStream("/exporter/configuration/valid-config.json")
                        .readAllBytes()));

        var configuration = csvImportProvider.loadConfiguration(createBatchImportConfig());

        Assertions.assertThat(configuration)
                .isNotNull()
                .extracting(
                        CSVConfiguration::delimiter,
                        CSVConfiguration::headers,
                        CSVConfiguration::dateFormat,
                        CSVConfiguration::transactionTypeIndicator,
                        CSVConfiguration::columnRoles)
                .isEqualTo(List.of(
                        ",",
                        true,
                        "yyyyMMdd",
                        new TransactionTypeIndicator("Bij", "Af"),
                        List.of(
                                ColumnRole.DATE,
                                ColumnRole.OPPOSING_NAME,
                                ColumnRole.ACCOUNT_IBAN,
                                ColumnRole.OPPOSING_IBAN,
                                ColumnRole.IGNORE,
                                ColumnRole.CUSTOM_INDICATOR,
                                ColumnRole.AMOUNT,
                                ColumnRole.IGNORE,
                                ColumnRole.DESCRIPTION)));
    }

    @Test
    @DisplayName("Read transactions with single deposit")
    void readTransactions_deposit() throws IOException {
        Mockito.when(storageService.read("my-secret-import-files"))
                .thenReturn(Control.Option(getClass()
                        .getResourceAsStream("/exporter/csv-files/single-deposit.csv")
                        .readAllBytes()));

        var consumer = Mockito.mock(TransactionConsumer.class);

        csvImportProvider.readTransactions(consumer, createCSVConfiguration(), createBatchImport());

        var transactionCaptor = ArgumentCaptor.forClass(TransactionDTO.class);
        Mockito.verify(consumer, Mockito.times(1)).accept(transactionCaptor.capture());

        var transaction = transactionCaptor.getValue();
        Assertions.assertThat(transaction.amount()).isEqualTo(14.19);
        Assertions.assertThat(transaction.opposingIBAN()).isEqualTo("NL69INGB0123454789");
        Assertions.assertThat(transaction.opposingName()).isEqualTo("Janssen PA");
        Assertions.assertThat(transaction.transactionDate()).isEqualTo("2016-05-31");
        Assertions.assertThat(transaction.type()).isEqualTo(TransactionType.DEBIT);
    }

    @Test
    @DisplayName("Read transactions with single withdrawal")
    void readTransactions_withdrawal() throws IOException {
        Mockito.when(storageService.read("my-secret-import-files"))
                .thenReturn(Control.Option(getClass()
                        .getResourceAsStream("/exporter/csv-files/single-withdrawal.csv")
                        .readAllBytes()));

        var consumer = Mockito.mock(TransactionConsumer.class);

        csvImportProvider.readTransactions(consumer, createCSVConfiguration(), createBatchImport());

        var transactionCaptor = ArgumentCaptor.forClass(TransactionDTO.class);
        Mockito.verify(consumer, Mockito.times(1)).accept(transactionCaptor.capture());

        var transaction = transactionCaptor.getValue();
        Assertions.assertThat(transaction.amount()).isEqualTo(283.90);
        Assertions.assertThat(transaction.opposingIBAN()).isEqualTo("NL71INGB0009876543");
        Assertions.assertThat(transaction.opposingName()).isEqualTo("MW GA Pieterse");
        Assertions.assertThat(transaction.transactionDate()).isEqualTo("2016-05-31");
        Assertions.assertThat(transaction.type()).isEqualTo(TransactionType.CREDIT);
    }

    @Test
    void exposesCorrectType() {
        Assertions.assertThat(csvImportProvider.getImporterType()).isEqualTo("CSVImportProvider");
    }

    private BatchImport createBatchImport() {
        return BatchImport.create(createBatchImportConfig(), "my-secret-import-files");
    }

    private CSVConfiguration createCSVConfiguration() throws IOException {
        return objectMapper.readValue(
                getClass().getResourceAsStream("/exporter//configuration/valid-config.json"),
                CSVConfiguration.class);
    }

    private BatchImportConfig createBatchImportConfig() {
        return BatchImportConfig.create("csv", "CSV Tester", "my-secret-files");
    }
}
