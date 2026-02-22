package com.jongsoft.finance.exporter.adapter.rest;

import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.adapter.api.TagProvider;
import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.adapter.api.TransactionScheduleProvider;
import com.jongsoft.finance.banking.adapter.rest.TransactionMapper;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.Tag;
import com.jongsoft.finance.banking.domain.model.TransactionSchedule;
import com.jongsoft.finance.banking.types.SystemAccountTypes;
import com.jongsoft.finance.budget.adapter.api.BudgetProvider;
import com.jongsoft.finance.budget.domain.model.Budget;
import com.jongsoft.finance.classification.adapter.api.CategoryProvider;
import com.jongsoft.finance.classification.adapter.rest.CategoryMapper;
import com.jongsoft.finance.contract.adapter.api.ContractProvider;
import com.jongsoft.finance.contract.domain.model.Contract;
import com.jongsoft.finance.core.adapter.api.StorageService;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.rest.ExportApi;
import com.jongsoft.finance.rest.model.*;

import io.micronaut.http.annotation.Controller;

import org.bouncycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class ExportController implements ExportApi {

    private final AccountProvider accountProvider;
    private final CategoryProvider categoryProvider;
    private final ContractProvider contractProvider;
    private final BudgetProvider budgetProvider;
    private final TagProvider tagProvider;
    private final TransactionProvider transactionProvider;
    private final TransactionScheduleProvider scheduleProvider;
    private final FilterProvider<TransactionProvider.FilterCommand> filterFactory;

    private final StorageService storageService;

    public ExportController(
            AccountProvider accountProvider,
            CategoryProvider categoryProvider,
            ContractProvider contractProvider,
            BudgetProvider budgetProvider,
            TagProvider tagProvider,
            TransactionProvider transactionProvider,
            TransactionScheduleProvider scheduleProvider,
            FilterProvider<TransactionProvider.FilterCommand> filterFactory,
            StorageService storageService) {
        this.accountProvider = accountProvider;
        this.categoryProvider = categoryProvider;
        this.contractProvider = contractProvider;
        this.budgetProvider = budgetProvider;
        this.tagProvider = tagProvider;
        this.transactionProvider = transactionProvider;
        this.scheduleProvider = scheduleProvider;
        this.filterFactory = filterFactory;
        this.storageService = storageService;
    }

    @Override
    public ExportProfileResponse exportUserAccount() {
        var response = new ExportProfileResponse();

        // todo convert Rules

        response.accounts(accountProvider
                .lookup()
                .filter(a -> !a.getType().equals(SystemAccountTypes.RECONCILE.label()))
                .map(this::toAccountResponse)
                .toJava());
        response.setCategories(categoryProvider
                .lookup()
                .map(CategoryMapper::toCategoryResponse)
                .toJava());
        response.setContract(
                contractProvider.lookup().map(this::toContractResponse).toJava());
        response.setBudget(budgetProvider.lookup().map(this::toBudgetResponse).toJava());
        response.setTags(tagProvider.lookup().map(Tag::name).toJava());
        response.setTransaction(lookupRelevantTransactions());
        response.setSchedules(
                scheduleProvider.lookup().map(this::toScheduleResponse).toJava());

        return response;
    }

    private ExportProfileResponseSchedulesInner toScheduleResponse(TransactionSchedule schedule) {
        return new ExportProfileResponseSchedulesInner()
                .id(schedule.getId())
                .description(schedule.getDescription())
                .name(schedule.getName())
                .transferBetween(new TransactionScheduleRequestTransferBetween(
                        new AccountLink(
                                schedule.getSource().getId(),
                                schedule.getSource().getName(),
                                schedule.getSource().getType()),
                        new AccountLink(
                                schedule.getDestination().getId(),
                                schedule.getDestination().getName(),
                                schedule.getDestination().getType())))
                .activeBetween(new DateRange(schedule.getStart(), schedule.getEnd()))
                .schedule(new ScheduleResponse(
                        Periodicity.fromValue(
                                schedule.getSchedule().periodicity().name()),
                        schedule.getSchedule().interval()))
                .lastRun(schedule.getLastRun())
                .nextRun(schedule.getNextRun());
    }

    private List<TransactionResponse> lookupRelevantTransactions() {
        // we also want to export all opening balance transactions for liability accounts
        var filter = filterFactory
                .create()
                .page(0, Integer.MAX_VALUE)
                .description("Opening balance", true);

        return transactionProvider
                .lookup(filter)
                .content()
                .map(TransactionMapper::toTransactionResponse)
                .toJava();
    }

    private ExportProfileResponseBudgetInner toBudgetResponse(Budget budget) {
        var response = new ExportProfileResponseBudgetInner();

        response.setIncome(budget.getExpectedIncome());
        response.period(new DateRange(budget.getStart(), budget.getEnd()));
        for (var expense : budget.getExpenses()) {
            var responseExpense = new ExportProfileResponseBudgetInnerExpensesInner();
            responseExpense.name(expense.getName());
            responseExpense.expected(BigDecimal.valueOf(expense.computeBudget()));
            response.addExpensesItem(responseExpense);
        }

        return response;
    }

    private ExportProfileResponseContractInner toContractResponse(Contract contract) {
        var response = new ExportProfileResponseContractInner(
                contract.getId(),
                contract.getName(),
                contract.getStartDate(),
                contract.getEndDate(),
                new AccountLink(
                        contract.getCompany().getId(),
                        contract.getCompany().getName(),
                        contract.getCompany().getType()));

        if (contract.getFileToken() != null) {
            response.contract(loadFromEncryptedStorage(contract.getFileToken()));
        }

        response.description(contract.getDescription());
        response.notification(contract.isNotifyBeforeEnd());
        response.terminated(contract.isTerminated());

        return response;
    }

    private ExportProfileResponseAccountsInner toAccountResponse(Account account) {
        var accountDetails = new AccountResponseAllOfAccount();
        var response = new ExportProfileResponseAccountsInner(
                account.getId(), account.getName(), account.getType(), accountDetails);

        accountDetails.currency(account.getCurrency());
        accountDetails.bic(account.getBic());
        accountDetails.iban(account.getIban());
        accountDetails.number(account.getNumber());

        response.description(account.getDescription());
        if (account.getInterestPeriodicity() != null) {
            response.interest(new AccountResponseAllOfInterest(
                    Periodicity.fromValue(account.getInterestPeriodicity().name()),
                    account.getInterest()));
        }

        if (account.getImageFileToken() != null) {
            response.setIcon(loadFromEncryptedStorage(account.getImageFileToken()));
        }

        return response;
    }

    private String loadFromEncryptedStorage(String fileToken) {
        var bytes = storageService.read(fileToken).getOrSupply(() -> new byte[0]);
        return Hex.toHexString(bytes);
    }
}
