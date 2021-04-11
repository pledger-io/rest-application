package com.jongsoft.finance.bpmn.delegate.importer;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.serialized.ImportConfigJson;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Optional;

@Slf4j
abstract class CSVReaderDelegate implements JavaDelegate {

    private final ImportProvider importProvider;
    private final StorageService storageService;

    protected CSVReaderDelegate(ImportProvider importProvider, StorageService storageService) {
        this.importProvider = importProvider;
        this.storageService = storageService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String batchImportSlug = (String) execution.getVariableLocal("batchImportSlug");
        ImportConfigJson importConfigJson = getFromContext(execution);

        BatchImport batchImport = importProvider.lookup(batchImportSlug).blockingGet();
        log.debug("{}: Processing transaction import CSV {}", execution.getCurrentActivityName(), batchImport.getSlug());
        if (importConfigJson == null) {
            throw new IllegalStateException("Cannot run account extraction without actual configuration.");
        }

        var mappingIndices = computeIndices(importConfigJson.getColumnRoles());
        var parserConfig = new CSVParserBuilder()
                .withSeparator(importConfigJson.getDelimiter())
                .build();

        beforeProcess(execution, importConfigJson);

        storageService.read(batchImport.getFileCode())
                .map(bytes -> new InputStreamReader(new ByteArrayInputStream(bytes)))
                .flatMapPublisher(stream -> Flowable.fromIterable(
                        new CSVReaderBuilder(stream)
                                .withSkipLines(importConfigJson.isHeaders() ? 1 : 0)
                                .withCSVParser(parserConfig)
                                .build()))
                .filter(line -> line.length > 1 || line.length <= mappingIndices.size())
                .map(csvLine -> this.transform(csvLine, mappingIndices, importConfigJson))
                .forEach(transaction -> this.lineRead(execution, transaction));

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

    private ParsedTransaction transform(
            String[] csvLine,
            EnumMap<ImportConfigJson.MappingRole, Integer> mappings,
            ImportConfigJson configJson) {
        var amount = parseAmount(mappings, csvLine);
        var date = parseDate(
                mappings,
                csvLine,
                configJson.getDateFormat(),
                ImportConfigJson.MappingRole.DATE);
        var bookDate = parseDate(
                mappings,
                csvLine,
                configJson.getDateFormat(),
                ImportConfigJson.MappingRole.BOOK_DATE);
        var interestDate = parseDate(
                mappings,
                csvLine,
                configJson.getDateFormat(),
                ImportConfigJson.MappingRole.INTEREST_DATE);
        var opposingName = locateColumn(csvLine, mappings, ImportConfigJson.MappingRole.OPPOSING_NAME);
        var opposingIBAN = locateColumn(csvLine, mappings, ImportConfigJson.MappingRole.OPPOSING_IBAN);
        var description = locateColumn(csvLine, mappings, ImportConfigJson.MappingRole.DESCRIPTION);

        Transaction.Type type = amount >= 0 ? Transaction.Type.DEBIT : Transaction.Type.CREDIT;
        if (mappings.containsKey(ImportConfigJson.MappingRole.CUSTOM_INDICATOR)) {
            type = parseType(csvLine, mappings, configJson.getCustomIndicator());
        }

        return new ParsedTransaction(
                amount,
                type,
                description,
                date,
                interestDate,
                bookDate,
                opposingIBAN,
                opposingName);
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
