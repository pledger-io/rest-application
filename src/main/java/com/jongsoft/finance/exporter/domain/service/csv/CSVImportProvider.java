package com.jongsoft.finance.exporter.domain.service.csv;

import com.jongsoft.finance.banking.types.TransactionType;
import com.jongsoft.finance.core.adapter.api.StorageService;
import com.jongsoft.finance.exporter.domain.model.BatchImport;
import com.jongsoft.finance.exporter.domain.model.BatchImportConfig;
import com.jongsoft.finance.exporter.domain.model.ImporterConfiguration;
import com.jongsoft.finance.exporter.domain.service.ImporterProvider;
import com.jongsoft.finance.exporter.domain.service.TransactionConsumer;
import com.jongsoft.finance.exporter.domain.service.TransactionDTO;
import com.jongsoft.lang.Control;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import io.micronaut.serde.ObjectMapper;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

@Singleton
class CSVImportProvider implements ImporterProvider<CSVConfiguration> {
    private final Logger logger = LoggerFactory.getLogger(CSVImportProvider.class);

    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    public CSVImportProvider(StorageService storageService, ObjectMapper objectMapper) {
        this.storageService = storageService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void readTransactions(
            TransactionConsumer consumer,
            ImporterConfiguration configuration,
            BatchImport importJob) {
        logger.info("Reading transactions from CSV file: {}", importJob.getSlug());
        var csvConfiguration = (CSVConfiguration) configuration;

        try {
            var inputStream = storageService
                    .read(importJob.getFileCode())
                    .map(ByteArrayInputStream::new)
                    .map(InputStreamReader::new)
                    .getOrThrow(() -> new IllegalStateException(
                            "Failed to read CSV file: " + importJob.getFileCode()));

            try (var reader = new CSVReaderBuilder(inputStream)
                    .withCSVParser(new CSVParserBuilder()
                            .withSeparator(csvConfiguration.delimiter().charAt(0))
                            .build())
                    .build()) {

                if (csvConfiguration.headers()) {
                    logger.debug("CSV file has headers, skipping first line");
                    reader.skip(1);
                }

                String[] line;
                while ((line = reader.readNext()) != null) {
                    if (line.length != csvConfiguration.columnRoles().size()) {
                        logger.warn(
                                "Skipping line, columns found {} but expected is {}: {}",
                                line.length,
                                csvConfiguration.columnRoles().size(),
                                line);
                        continue;
                    }

                    consumer.accept(readLine(line, csvConfiguration));
                }
            }
        } catch (IOException | CsvValidationException e) {
            logger.warn("Failed to read CSV file: {}", importJob.getFileCode(), e);
        }
    }

    @Override
    public CSVConfiguration loadConfiguration(BatchImportConfig batchImportConfig) {
        logger.debug("Loading CSV configuration from disk: {}", batchImportConfig.getFileCode());

        try {
            var jsonBytes = storageService.read(batchImportConfig.getFileCode());
            if (jsonBytes.isPresent()) {
                return objectMapper.readValue(jsonBytes.get(), CSVConfiguration.class);
            }

            logger.warn("No CSV configuration found on disk: {}", batchImportConfig.getFileCode());
            throw new IllegalStateException(
                    "No CSV configuration found on disk: " + batchImportConfig.getFileCode());
        } catch (IOException e) {
            logger.warn(
                    "Could not load CSV configuration from disk: {}",
                    batchImportConfig.getFileCode(),
                    e);
            throw new IllegalStateException("Failed to load CSV configuration from disk: "
                    + batchImportConfig.getFileCode());
        }
    }

    private TransactionDTO readLine(String[] line, CSVConfiguration configuration) {
        Function<ColumnRole, String> columnLocator =
                (role) -> Control.Try(() -> line[configuration.columnRoles().indexOf(role)])
                        .recover(_ -> null)
                        .get();
        Function<String, LocalDate> parseDate = (date) -> date != null
                ? LocalDate.parse(date, DateTimeFormatter.ofPattern(configuration.dateFormat()))
                : null;
        Function<String, Double> parseAmount =
                (amount) -> Double.parseDouble(amount.replace(',', '.'));

        var amount = parseAmount.apply(columnLocator.apply(ColumnRole.AMOUNT));
        var type = Control.Option(columnLocator.apply(ColumnRole.CUSTOM_INDICATOR))
                .map(indicator -> {
                    if (indicator.equalsIgnoreCase(
                            configuration.transactionTypeIndicator().credit())) {
                        return TransactionType.CREDIT;
                    } else if (indicator.equalsIgnoreCase(
                            configuration.transactionTypeIndicator().deposit())) {
                        return TransactionType.DEBIT;
                    }

                    return null;
                })
                .getOrSupply(() -> amount >= 0 ? TransactionType.DEBIT : TransactionType.CREDIT);

        logger.trace(
                "Reading single transaction on {}: amount={}, type={}",
                parseDate.apply(columnLocator.apply(ColumnRole.DATE)),
                amount,
                type);

        return new TransactionDTO(
                amount,
                type,
                columnLocator.apply(ColumnRole.DESCRIPTION),
                parseDate.apply(columnLocator.apply(ColumnRole.DATE)),
                parseDate.apply(columnLocator.apply(ColumnRole.INTEREST_DATE)),
                parseDate.apply(columnLocator.apply(ColumnRole.BOOK_DATE)),
                columnLocator.apply(ColumnRole.OPPOSING_IBAN),
                columnLocator.apply(ColumnRole.OPPOSING_NAME),
                columnLocator.apply(ColumnRole.BUDGET),
                columnLocator.apply(ColumnRole.CATEGORY),
                List.of());
    }
}
