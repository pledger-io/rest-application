package com.jongsoft.finance.exporter.adapter.rest;

import static java.lang.Long.parseLong;

import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.adapter.api.TransactionScheduleProvider;
import com.jongsoft.finance.banking.domain.commands.CreateTransactionCommand;
import com.jongsoft.finance.banking.domain.model.*;
import com.jongsoft.finance.banking.types.TransactionLinkType;
import com.jongsoft.finance.banking.types.TransactionType;
import com.jongsoft.finance.budget.adapter.api.BudgetProvider;
import com.jongsoft.finance.budget.adapter.api.ExpenseProvider;
import com.jongsoft.finance.budget.domain.model.Budget;
import com.jongsoft.finance.classification.adapter.api.CategoryProvider;
import com.jongsoft.finance.classification.domain.model.Category;
import com.jongsoft.finance.contract.adapter.api.ContractProvider;
import com.jongsoft.finance.contract.domain.model.Contract;
import com.jongsoft.finance.core.adapter.api.StorageService;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.core.value.Periodicity;
import com.jongsoft.finance.rest.ImportApi;
import com.jongsoft.finance.rest.model.*;
import com.jongsoft.finance.suggestion.adapter.api.TransactionRuleProvider;
import com.jongsoft.finance.suggestion.domain.commands.CreateRuleGroupCommand;
import com.jongsoft.finance.suggestion.domain.model.TransactionRule;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.Collectors;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
class ImportController implements ImportApi {

    private final Logger log = LoggerFactory.getLogger(ImportController.class);

    private final StorageService storageService;
    private final AccountProvider accountProvider;
    private final ContractProvider contractProvider;
    private final CategoryProvider categoryProvider;
    private final BudgetProvider budgetProvider;
    private final TransactionScheduleProvider scheduleProvider;
    private final TransactionCreationHandler transactionCreationHandler;
    private final TransactionProvider transactionProvider;
    private final ExpenseProvider expenseProvider;
    private final TransactionRuleProvider transactionRuleProvider;

    private final FilterProvider<ExpenseProvider.FilterCommand> expenseFilterFactory;

    ImportController(
            StorageService storageService,
            AccountProvider accountProvider,
            ContractProvider contractProvider,
            CategoryProvider categoryProvider,
            BudgetProvider budgetProvider,
            TransactionScheduleProvider scheduleProvider,
            TransactionCreationHandler transactionCreationHandler,
            TransactionProvider transactionProvider,
            ExpenseProvider expenseProvider,
            TransactionRuleProvider transactionRuleProvider,
            FilterProvider<ExpenseProvider.FilterCommand> expenseFilterFactory) {
        this.storageService = storageService;
        this.accountProvider = accountProvider;
        this.contractProvider = contractProvider;
        this.categoryProvider = categoryProvider;
        this.budgetProvider = budgetProvider;
        this.scheduleProvider = scheduleProvider;
        this.transactionCreationHandler = transactionCreationHandler;
        this.transactionProvider = transactionProvider;
        this.expenseProvider = expenseProvider;
        this.transactionRuleProvider = transactionRuleProvider;
        this.expenseFilterFactory = expenseFilterFactory;
    }

    @Override
    public HttpResponse<Void> importUserAccount(ExportProfileResponse profile) {
        Map<Long, Long> accountMap = new HashMap<>();
        Map<Long, Long> categoryMap = new HashMap<>();
        Map<Long, Long> contractMap = new HashMap<>();
        if (profile.getAccounts() != null) {
            profile.getAccounts()
                    .forEach(account -> accountMap.put(account.getId(), importAccount(account)));
        }
        if (profile.getCategories() != null) {
            profile.getCategories()
                    .forEach(category ->
                            categoryMap.put(category.getId(), importCategory(category)));
        }
        if (profile.getTags() != null) {
            profile.getTags().forEach(tag -> Tag.create(tag.trim()));
        }

        if (profile.getContract() != null) {
            profile.getContract()
                    .forEach(contract -> contractMap.put(
                            contract.getId(), importContract(accountMap, contract)));
        }

        if (profile.getBudget() != null) {
            profile.getBudget().stream()
                    .sorted(Comparator.comparing(l -> l.getPeriod().getStartDate()))
                    .forEach(this::importBudget);
        }

        if (profile.getSchedules() != null) {
            profile.getSchedules().forEach(schedule -> importSchedule(accountMap, schedule));
        }

        if (profile.getRuleGroups() != null) {
            profile.getRuleGroups()
                    .forEach(ruleGroup ->
                            importRuleGroup(contractMap, categoryMap, accountMap, ruleGroup));
        }

        if (profile.getTransaction() != null) {
            profile.getTransaction()
                    .forEach(transaction -> importTransaction(accountMap, transaction));
        }

        return HttpResponse.noContent();
    }

    private void importTransaction(
            Map<Long, Long> accountMap, TransactionResponse importTransaction) {
        var transactionId =
                transactionCreationHandler.handleCreatedEvent(new CreateTransactionCommand(
                        importTransaction.getDates().getTransaction(),
                        importTransaction.getDescription(),
                        TransactionType.valueOf(importTransaction.getType().name()),
                        null,
                        importTransaction.getCurrency(),
                        accountMap.get(importTransaction.getSource().getId()),
                        accountMap.get(importTransaction.getDestination().getId()),
                        BigDecimal.valueOf(importTransaction.getAmount())));

        var createdTransaction = transactionProvider.lookup(transactionId).get();
        if (importTransaction.getMetadata() != null) {
            if (importTransaction.getMetadata().getCategory() != null) {
                var category = categoryProvider
                        .lookup(importTransaction.getMetadata().getCategory())
                        .get();
                createdTransaction.link(TransactionLinkType.CATEGORY, category.getId());
            }
            if (importTransaction.getMetadata().getBudget() != null) {
                var expense = expenseProvider
                        .lookup(expenseFilterFactory
                                .create()
                                .name(importTransaction.getMetadata().getBudget(), true))
                        .content()
                        .get();
                createdTransaction.link(TransactionLinkType.EXPENSE, expense.getId());
            }

            if (importTransaction.getMetadata().getContract() != null) {
                var contract = contractProvider
                        .lookup(importTransaction.getMetadata().getContract())
                        .get();
                createdTransaction.link(TransactionLinkType.CONTRACT, contract.getId());
            }

            if (importTransaction.getMetadata().getTags() != null) {
                createdTransaction.tag(
                        Collections.List(importTransaction.getMetadata().getTags()));
            }
        }

        if (importTransaction.getSplit() != null) {
            createdTransaction.split(importTransaction.getSplit().stream()
                    .map(split -> new SplitRecord(split.getDescription(), split.getAmount()))
                    .collect(Collectors.toList()));
        }
    }

    private void importSchedule(
            Map<Long, Long> accountMap, ExportProfileResponseSchedulesInner importSchedule) {
        log.trace("Importing schedule {}.", importSchedule.getName());
        var sourceAccount = accountProvider
                .lookup(accountMap.get(
                        importSchedule.getTransferBetween().getSource().getId()))
                .get();
        var targetAccount = accountProvider
                .lookup(accountMap.get(
                        importSchedule.getTransferBetween().getDestination().getId()))
                .get();

        TransactionSchedule.create(
                importSchedule.getName(),
                new ScheduleValue(
                        Periodicity.valueOf(
                                importSchedule.getSchedule().getPeriodicity().name()),
                        importSchedule.getSchedule().getInterval()),
                sourceAccount,
                targetAccount,
                importSchedule.getAmount());

        var createdSchedule = scheduleProvider
                .lookup()
                .filter(schedule -> schedule.getName().equals(importSchedule.getName()))
                .get();

        if (importSchedule.getActiveBetween() != null
                && importSchedule.getActiveBetween().getStartDate() != null) {
            createdSchedule.limit(
                    importSchedule.getActiveBetween().getStartDate(),
                    importSchedule.getActiveBetween().getEndDate());
            createdSchedule.reschedule();
        }
    }

    private void importBudget(ExportProfileResponseBudgetInner importBudget) {
        log.trace("Importing budget from {}.", importBudget.getPeriod().getStartDate());

        var budgetStart = importBudget.getPeriod().getStartDate();
        var existingBudget =
                budgetProvider.lookup(budgetStart.getYear(), budgetStart.getMonthValue());
        if (!existingBudget.isPresent()) {
            Budget.create(budgetStart, importBudget.getIncome()).activate();
        } else {
            existingBudget.get().indexBudget(budgetStart, importBudget.getIncome());
        }

        var updatedBudget = budgetProvider
                .lookup(budgetStart.getYear(), budgetStart.getMonthValue())
                .get();
        for (var expense : importBudget.getExpenses()) {
            var existingExpense = updatedBudget.determineExpense(expense.getName());
            if (existingExpense == null) {
                updatedBudget.createExpense(
                        expense.getName(),
                        expense.getExpected().min(BigDecimal.ONE).doubleValue(),
                        expense.getExpected().doubleValue());
            } else {
                existingExpense.updateExpense(expense.getExpected().doubleValue());
            }
        }
    }

    private long importContract(
            Map<Long, Long> accountMap, ExportProfileResponseContractInner importContract) {
        log.trace("Importing contract {}.", importContract.getName());
        var company = accountProvider
                .lookup(accountMap.get(importContract.getCompany().getId()))
                .get();

        Contract.create(
                company,
                importContract.getName(),
                importContract.getDescription(),
                importContract.getStart(),
                importContract.getEnd());

        var createdContract = contractProvider.lookup(importContract.getName()).get();
        if (importContract.getContract() != null) {
            var fileCode = saveToEncryptedStorage(importContract.getContract());
            createdContract.registerUpload(fileCode);
        }

        if (Objects.equals(importContract.getTerminated(), Boolean.TRUE)) {
            createdContract.terminate();
        } else if (Objects.equals(importContract.getNotification(), Boolean.TRUE)) {
            createdContract.warnBeforeExpires();
        }

        return createdContract.getId();
    }

    private long importCategory(CategoryResponse importCategory) {
        log.trace("Importing category {}.", importCategory.getName());
        Category.create(importCategory.getName(), importCategory.getDescription());
        return categoryProvider.lookup(importCategory.getName()).get().getId();
    }

    private long importAccount(ExportProfileResponseAccountsInner importAccount) {
        log.trace("Importing account {}.", importAccount.getName());
        Account.create(
                null,
                importAccount.getName(),
                importAccount.getAccount().getCurrency(),
                importAccount.getType());

        var createdAccount = accountProvider.lookup(importAccount.getName()).get();
        createdAccount.changeAccount(
                importAccount.getAccount().getIban(),
                importAccount.getAccount().getBic(),
                importAccount.getAccount().getNumber());
        createdAccount.rename(
                importAccount.getName(),
                importAccount.getDescription(),
                importAccount.getAccount().getCurrency(),
                importAccount.getType());

        if (importAccount.getInterest() != null) {
            createdAccount.interest(
                    importAccount.getInterest().getInterest(),
                    Periodicity.valueOf(
                            importAccount.getInterest().getPeriodicity().name()));
        }

        if (importAccount.getIcon() != null) {
            var iconFileCode = saveToEncryptedStorage(importAccount.getIcon());
            createdAccount.registerIcon(iconFileCode);
        }

        return createdAccount.getId();
    }

    private void importRuleGroup(
            Map<Long, Long> contractMap,
            Map<Long, Long> categoryMap,
            Map<Long, Long> accountMap,
            ExportProfileResponseRuleGroupsInner ruleGroup) {
        CreateRuleGroupCommand.ruleGroupCreated(ruleGroup.getName());

        for (var rule : ruleGroup.getRules()) {
            var toCreate = TransactionRule.create(rule.getName(), rule.getRestrictive());
            toCreate.assign(ruleGroup.getName());
            toCreate.change(
                    rule.getName(), rule.getDescription(), rule.getRestrictive(), rule.getActive());
            for (var change : rule.getChanges()) {
                var updatedChange =
                        switch (change.getField()) {
                            case BUDGET -> null; // todo find a way to map the budget expense id
                            case CATEGORY ->
                                categoryMap.get(parseLong(change.getChange())).toString();
                            case CONTRACT ->
                                contractMap.get(parseLong(change.getChange())).toString();
                            case SOURCE_ACCOUNT, TO_ACCOUNT, CHANGE_TRANSFER_TO ->
                                accountMap.get(parseLong(change.getChange())).toString();
                            default -> change.getChange();
                        };
                toCreate.registerChange(change.getField(), updatedChange);
            }
            for (var condition : rule.getConditions()) {
                toCreate.registerCondition(
                        condition.getField(), condition.getOperation(), condition.getCondition());
            }
            transactionRuleProvider.save(toCreate);
        }
    }

    private String saveToEncryptedStorage(String hexEncodedBytes) {
        var bytes = Hex.decode(hexEncodedBytes);
        return storageService.store(bytes);
    }
}
