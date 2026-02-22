package com.jongsoft.finance.exporter.domain.service.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.domain.commands.CreateTransactionCommand;
import com.jongsoft.finance.banking.domain.model.TransactionCreationHandler;
import com.jongsoft.finance.core.adapter.api.StorageService;
import com.jongsoft.finance.core.domain.model.ProcessVariable;
import com.jongsoft.finance.exporter.domain.model.AccountMapping;
import com.jongsoft.finance.exporter.domain.model.BatchImport;
import com.jongsoft.finance.exporter.domain.model.ProcessConfiguration;
import com.jongsoft.finance.exporter.domain.model.UserTask;
import com.jongsoft.finance.exporter.domain.service.ImporterProvider;
import com.jongsoft.finance.exporter.domain.service.TransactionDTO;
import com.jongsoft.finance.exporter.types.ProcessingStage;
import com.jongsoft.finance.suggestion.domain.commands.ApplyTransactionRulesCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Function;

public class ImportProcess {

    private final Logger log = LoggerFactory.getLogger(ImportProcess.class);

    private ImportContext importContext;

    private final Function<String, Long> accountResolver;
    private final BatchImport batchImport;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    public ImportProcess(
            Function<String, Long> accountResolver,
            BatchImport batchImport,
            StorageService storageService) {
        this.accountResolver = accountResolver;
        this.batchImport = batchImport;
        this.storageService = storageService;
        this.objectMapper = new JsonMapper();
    }

    public boolean isWaiting() {
        return importContext.isPendingUserAction();
    }

    public void process(
            List<ImporterProvider<?>> importProviders,
            Function<Long, String> locateCurrency,
            TransactionCreationHandler transactionCreationHandler) {
        ImporterProvider<?> relevantProvider = importProviders.stream()
                .filter(provider -> provider.getImporterType()
                        .equals(batchImport.getConfig().getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No importer found for format: "
                        + batchImport.getConfig().getType()));

        ProcessingStage nextStage =
                switch (importContext.getCurrentStage()) {
                    case CONFIGURATION, MISSING_ACCOUNT, COMPLETED ->
                        importContext.getCurrentStage();
                    case ACCOUNT_MAPPING -> processAccountMapping(relevantProvider);
                    case IMPORTING ->
                        processImporting(
                                relevantProvider, locateCurrency, transactionCreationHandler);
                };
        importContext.setCurrentStage(nextStage);
    }

    public void loadContext(List<ImporterProvider<?>> importProviders) throws IOException {
        ImporterProvider<?> relevantProvider = importProviders.stream()
                .filter(provider -> provider.getImporterType()
                        .equals(batchImport.getConfig().getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No importer found for format: "
                        + batchImport.getConfig().getType()));

        Path jobLocation = storageService.getUploadPath().resolve(batchImport.getSlug());
        if (Files.exists(jobLocation.resolve("context.json"))) {
            log.debug("Loading context from {}", jobLocation);
            byte[] contextBytes =
                    storageService.read(batchImport.getSlug() + "/context.json").get();
            importContext = objectMapper.readValue(contextBytes, ImportContext.class);
        } else {
            log.debug("No context found, starting configuration process.");
            importContext = new ImportContext();
            var importConfig = relevantProvider.loadConfiguration(batchImport.getConfig());
            importContext.setConfiguration(
                    new ProcessConfiguration(importConfig, null, false, false));
            importContext.waitForUser();
        }
    }

    public void saveContext() throws IOException {
        if (!Files.exists(storageService.getUploadPath().resolve(batchImport.getSlug()))) {
            Files.createDirectories(storageService.getUploadPath().resolve(batchImport.getSlug()));
        }

        String fileCode = storageService.store(objectMapper.writeValueAsBytes(importContext));
        Files.move(
                storageService.getUploadPath().resolve(fileCode),
                storageService.getUploadPath().resolve(batchImport.getSlug() + "/context.json"),
                StandardCopyOption.REPLACE_EXISTING);
    }

    public List<UserTask> computeUserTasks() {
        ProcessingStage currentStage = importContext.getCurrentStage();

        if (currentStage == ProcessingStage.CONFIGURATION) {
            return List.of(new UserTask(
                    "configuration", "configuration", importContext.getConfiguration()));
        }

        if (currentStage == ProcessingStage.MISSING_ACCOUNT) {
            List<UserTask> mappingTasks = new ArrayList<>();
            for (var accountName : importContext.getAccountMapping().keySet()) {
                if (importContext.getAccountMapping().get(accountName) != null) {
                    continue;
                }
                mappingTasks.add(new UserTask(
                        accountName, "account-mapping", new AccountMapping(accountName, null)));
            }
            return mappingTasks;
        }

        throw StatusException.notFound("No tasks available");
    }

    public void completeTask(String taskId, ProcessVariable userData) {
        ProcessingStage currentStage = importContext.getCurrentStage();

        if (currentStage == ProcessingStage.CONFIGURATION) {
            if (userData instanceof ProcessConfiguration configuration) {
                if (configuration.accountId() == null) {
                    throw StatusException.badRequest("Missing account id.");
                }

                importContext.userReplied();
                importContext.setConfiguration(configuration);
                importContext.setCurrentStage(ProcessingStage.ACCOUNT_MAPPING);
            } else {
                throw StatusException.badRequest("Missing configuration.");
            }
        }

        if (currentStage == ProcessingStage.MISSING_ACCOUNT) {
            if (userData instanceof AccountMapping(String name, Long accountId)) {
                if (accountId == null) {
                    throw StatusException.badRequest("Missing account id.");
                }
                importContext.addMapping(name, accountId);
                if (!importContext.hasMissingAccounts()) {
                    importContext.userReplied();
                    importContext.setCurrentStage(ProcessingStage.IMPORTING);
                }
            } else {
                throw StatusException.badRequest("Missing account mapping.");
            }
        }
    }

    private ProcessingStage processAccountMapping(ImporterProvider<?> relevantProvider) {
        var transactions = readTransactions(relevantProvider);

        transactions.stream()
                .map(TransactionDTO::opposingName)
                .distinct()
                .forEach(name -> importContext.addMapping(name, accountResolver.apply(name)));

        if (importContext.hasMissingAccounts()) {
            log.debug("Missing accounts: {}", importContext.getAccountMapping().size());
            importContext.waitForUser();
            return ProcessingStage.MISSING_ACCOUNT;
        }

        log.debug("Account mapping complete, starting import process.");
        return ProcessingStage.IMPORTING;
    }

    private ProcessingStage processImporting(
            ImporterProvider<?> relevantProvider,
            Function<Long, String> locateCurrency,
            TransactionCreationHandler transactionCreationHandler) {
        String currency = locateCurrency.apply(importContext.getConfiguration().accountId());
        for (TransactionDTO transaction : readTransactions(relevantProvider)) {
            long accountId = importContext.locateAccount(transaction.opposingName());

            var transactionId =
                    transactionCreationHandler.handleCreatedEvent(new CreateTransactionCommand(
                            transaction.transactionDate(),
                            transaction.description(),
                            transaction.type(),
                            null,
                            currency,
                            importContext.getConfiguration().accountId(),
                            accountId,
                            BigDecimal.valueOf(transaction.amount())));
            if (importContext.getConfiguration().applyRules()) {
                ApplyTransactionRulesCommand.applyTransactionRules(transactionId);
            }
        }

        batchImport.finish(new Date());
        return ProcessingStage.COMPLETED;
    }

    private List<TransactionDTO> readTransactions(ImporterProvider<?> relevantProvider) {
        List<TransactionDTO> transactions = new ArrayList<>();
        relevantProvider.readTransactions(
                transactions::add,
                importContext.getConfiguration().importerConfiguration(),
                batchImport);
        return transactions;
    }
}
