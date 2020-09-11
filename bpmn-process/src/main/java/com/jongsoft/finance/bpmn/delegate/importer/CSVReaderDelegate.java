package com.jongsoft.finance.bpmn.delegate.importer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.importer.ImportProvider;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.serialized.ImportConfigJson;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class CSVReaderDelegate implements JavaDelegate {

    private final ImportProvider importProvider;
    private final StorageService storageService;

    public CSVReaderDelegate(ImportProvider importProvider, StorageService storageService) {
        this.importProvider = importProvider;
        this.storageService = storageService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String batchImportSlug = (String) execution.getVariableLocal("batchImportSlug");
        ImportConfigJson importConfigJson = getFromContext(execution);

        BatchImport batchImport = importProvider.lookup(batchImportSlug).get();
        log.debug("{}: Processing transaction import CSV {}", execution.getCurrentActivityName(), batchImport.getSlug());
        if (importConfigJson == null) {
            throw new IllegalStateException("Cannot run account extraction without actual configuration.");
        }

        EnumMap<ImportConfigJson.MappingRole, Integer> mappingIndices = computeIndices(importConfigJson.getColumnRoles());

        CSVParser parserConfig = new CSVParserBuilder()
                .withSeparator(importConfigJson.getDelimiter())
                .build();

        Reader inputStreamReader = new InputStreamReader(new ByteArrayInputStream(storageService.read(batchImport.getFileCode())));
        CSVReader csvReader = new CSVReaderBuilder(inputStreamReader)
                .withSkipLines(importConfigJson.isHeaders() ? 1 : 0)
                .withCSVParser(parserConfig)
                .build();

        beforeProcess(execution, importConfigJson);
        try {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length == 1) {
                    log.trace("Skipping blank line for batch {}", batchImport.getSlug());
                    continue;
                } else if (line.length <= mappingIndices.size()) {
                    throw new IllegalStateException("Cannot run import CSV contains less columns then configured.");
                }

                var amount = parseAmount(mappingIndices, line);
                var date = parseDate(
                        mappingIndices,
                        line,
                        importConfigJson.getDateFormat(),
                        ImportConfigJson.MappingRole.DATE);
                var bookDate = parseDate(
                        mappingIndices,
                        line,
                        importConfigJson.getDateFormat(),
                        ImportConfigJson.MappingRole.BOOK_DATE);
                var interestDate = parseDate(
                        mappingIndices,
                        line,
                        importConfigJson.getDateFormat(),
                        ImportConfigJson.MappingRole.INTEREST_DATE);
                var opposingName = locateColumn(line, mappingIndices, ImportConfigJson.MappingRole.OPPOSING_NAME);
                var opposingIBAN = locateColumn(line, mappingIndices, ImportConfigJson.MappingRole.OPPOSING_IBAN);
                var description = locateColumn(line, mappingIndices, ImportConfigJson.MappingRole.DESCRIPTION);

                Transaction.Type type = amount >= 0 ? Transaction.Type.DEBIT : Transaction.Type.CREDIT;
                if (mappingIndices.containsKey(ImportConfigJson.MappingRole.CUSTOM_INDICATOR)) {
                    type = parseType(line, mappingIndices, importConfigJson.getCustomIndicator());
                }

                lineRead(
                        execution,
                        new ParsedTransaction(
                                amount,
                                type,
                                description,
                                date,
                                interestDate,
                                bookDate,
                                opposingIBAN,
                                opposingName));
            }
        } catch (IOException e) {
            log.warn("Failed to parse the CSV file for batch " + batchImport.getSlug(), e);
            throw new IllegalStateException("Failed to parse provided import.");
        }

        afterProcess(execution);
    }

    protected abstract void beforeProcess(DelegateExecution execution, ImportConfigJson configJson);
    protected abstract void lineRead(DelegateExecution execution, ParsedTransaction parsedTransaction);
    protected abstract void afterProcess(DelegateExecution execution);

    private ImportConfigJson getFromContext(DelegateExecution execution) {
        Object rawEntity = execution.getVariable("importConfig");
        if (rawEntity instanceof String) {
            return ImportConfigJson.read(rawEntity.toString());
        } else if (rawEntity instanceof ImportConfigJson) {
            return (ImportConfigJson) rawEntity;
        }

        throw new IllegalArgumentException("Unsupported import configuration provided.");
    }

    private EnumMap<ImportConfigJson.MappingRole, Integer> computeIndices(
            Iterable<ImportConfigJson.MappingRole> columnRoles) {
        EnumMap<ImportConfigJson.MappingRole, Integer> mapConfig =
                new EnumMap<>(ImportConfigJson.MappingRole.class);

        var i = 0;
        for (ImportConfigJson.MappingRole role : columnRoles) {
            mapConfig.put(role, i);
            i++;
        }

        return mapConfig;
    }

    private LocalDate parseDate(
            EnumMap<ImportConfigJson.MappingRole, Integer> mappingIndices, String[] line,
            String format, ImportConfigJson.MappingRole role) {
        return Optional.ofNullable(locateColumn(line, mappingIndices, role))
                .map(str -> LocalDate.parse(str, DateTimeFormatter.ofPattern(format)))
                .orElse(null);
    }

    private Transaction.Type parseType(
            String[] line, EnumMap<ImportConfigJson.MappingRole, Integer> mappingIndices,
            ImportConfigJson.CustomIndicator customIndicator) {
        return Optional.ofNullable(locateColumn(line, mappingIndices, ImportConfigJson.MappingRole.CUSTOM_INDICATOR))
                .map(str -> {
                    if (customIndicator.getCredit().equalsIgnoreCase(str)) {
                        return Transaction.Type.CREDIT;
                    } else {
                        return Transaction.Type.DEBIT;
                    }
                }).orElseThrow(() -> new IllegalStateException("Incorrect custom indicator found"));
    }

    private double parseAmount(
            EnumMap<ImportConfigJson.MappingRole, Integer> mappingIndices,
            String[] line) {
        String amount = locateColumn(line, mappingIndices, ImportConfigJson.MappingRole.AMOUNT);
        if (StringUtils.isEmpty(amount)) {
            throw new IllegalStateException("Amount cannot be blank in import");
        }

        return Double.parseDouble(amount.replace(',', '.'));
    }

    private String locateColumn(
            String[] line, EnumMap<ImportConfigJson.MappingRole, Integer> mappingIndices,
            ImportConfigJson.MappingRole column) {
        if (mappingIndices.containsKey(column)) {
            return line[mappingIndices.get(column)];
        }

        return null;
    }
}
