package com.jongsoft.finance.exporter.adapter.rest;

import static java.util.Optional.ofNullable;

import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.adapter.api.TagProvider;
import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.adapter.api.TransactionScheduleProvider;
import com.jongsoft.finance.banking.adapter.rest.TransactionMapper;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.Tag;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.banking.domain.model.TransactionSchedule;
import com.jongsoft.finance.banking.types.SystemAccountTypes;
import com.jongsoft.finance.banking.types.TransactionLinkType;
import com.jongsoft.finance.budget.adapter.api.BudgetProvider;
import com.jongsoft.finance.budget.domain.model.Budget;
import com.jongsoft.finance.classification.adapter.api.CategoryProvider;
import com.jongsoft.finance.classification.adapter.rest.CategoryMapper;
import com.jongsoft.finance.contract.adapter.api.ContractProvider;
import com.jongsoft.finance.contract.domain.model.Contract;
import com.jongsoft.finance.core.adapter.api.StorageService;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.core.domain.commands.InternalAuthenticationEvent;
import com.jongsoft.finance.exporter.annotations.ExporterModuleEnabled;
import com.jongsoft.finance.rest.ExportApi;
import com.jongsoft.finance.rest.model.*;
import com.jongsoft.finance.suggestion.adapter.api.TransactionRuleGroupProvider;
import com.jongsoft.finance.suggestion.adapter.api.TransactionRuleProvider;
import com.jongsoft.finance.suggestion.adapter.rest.RuleMapper;
import com.jongsoft.finance.suggestion.domain.model.TransactionRuleGroup;

import io.micronaut.core.io.Writable;
import io.micronaut.http.annotation.Controller;

import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@ExporterModuleEnabled
public class ExportController implements ExportApi {
    private final Logger log = LoggerFactory.getLogger(ExportController.class);

    private final AuthenticationFacade authenticationFacade;

    private final AccountProvider accountProvider;
    private final CategoryProvider categoryProvider;
    private final ContractProvider contractProvider;
    private final BudgetProvider budgetProvider;
    private final TagProvider tagProvider;
    private final TransactionProvider transactionProvider;
    private final TransactionScheduleProvider scheduleProvider;
    private final TransactionRuleProvider transactionRuleProvider;
    private final TransactionRuleGroupProvider transactionRuleGroupProvider;
    private final FilterProvider<TransactionProvider.FilterCommand> filterFactory;

    private final StorageService storageService;

    public ExportController(
            AuthenticationFacade authenticationFacade,
            AccountProvider accountProvider,
            CategoryProvider categoryProvider,
            ContractProvider contractProvider,
            BudgetProvider budgetProvider,
            TagProvider tagProvider,
            TransactionProvider transactionProvider,
            TransactionScheduleProvider scheduleProvider,
            TransactionRuleProvider transactionRuleProvider,
            TransactionRuleGroupProvider transactionRuleGroupProvider,
            FilterProvider<TransactionProvider.FilterCommand> filterFactory,
            StorageService storageService) {
        this.authenticationFacade = authenticationFacade;
        this.accountProvider = accountProvider;
        this.categoryProvider = categoryProvider;
        this.contractProvider = contractProvider;
        this.budgetProvider = budgetProvider;
        this.tagProvider = tagProvider;
        this.transactionProvider = transactionProvider;
        this.scheduleProvider = scheduleProvider;
        this.transactionRuleProvider = transactionRuleProvider;
        this.transactionRuleGroupProvider = transactionRuleGroupProvider;
        this.filterFactory = filterFactory;
        this.storageService = storageService;
    }

    @Override
    public ExportProfileResponse exportUserAccount() {
        log.info("Exporting user account.");
        var response = new ExportProfileResponse();

        response.setRuleGroups(transactionRuleGroupProvider
                .lookup()
                .map(this::convertRuleGroup)
                .toJava());

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

    @Override
    public Writable exportTransactions() {
        log.info("Exporting transactions.");
        String userAccount = authenticationFacade.authenticated();
        return outputStream -> {
            try {
                InternalAuthenticationEvent.authenticate(userAccount);
                BufferedWriter buffered = new BufferedWriter(outputStream);
                buffered.write(
                        "Date,Booking Date,Interest Date,From name,From IBAN,To name,To IBAN,Description,Category,Budget,Contract,Amount\n");

                int currentPage = 0;
                var filter = filterFactory.create().ownAccounts().page(currentPage, 25);
                var transactions = transactionProvider.lookup(filter);
                do {
                    log.debug("Processing page {} of transactions", currentPage);
                    for (Transaction transaction : transactions.content()) {
                        buffered.write(csv(transaction.getDate().toString()));
                        buffered.write(',');
                        buffered.write(csv(ofNullable(transaction.getBookDate())
                                .map(LocalDate::toString)
                                .orElse("")));
                        buffered.write(',');
                        buffered.write(csv(ofNullable(transaction.getInterestDate())
                                .map(LocalDate::toString)
                                .orElse("")));
                        buffered.write(',');
                        buffered.write(csv(transaction.computeFrom().getName()));
                        buffered.write(',');
                        buffered.write(csv(
                                ofNullable(transaction.computeFrom().getIban()).orElse("")));
                        buffered.write(',');
                        buffered.write(csv(transaction.computeTo().getName()));
                        buffered.write(',');
                        buffered.write(csv(
                                ofNullable(transaction.computeTo().getIban()).orElse("")));
                        buffered.write(',');
                        buffered.write(csv(transaction.getDescription()));
                        buffered.write(',');
                        buffered.write(csv(ofNullable(transaction
                                        .getMetadata()
                                        .get(TransactionLinkType.CATEGORY.name()))
                                .map(Object::toString)
                                .orElse("")));
                        buffered.write(',');
                        buffered.write(csv(ofNullable(transaction
                                        .getMetadata()
                                        .get(TransactionLinkType.EXPENSE.name()))
                                .map(Object::toString)
                                .orElse("")));
                        buffered.write(',');
                        buffered.write(csv(ofNullable(transaction
                                        .getMetadata()
                                        .get(TransactionLinkType.CONTRACT.name()))
                                .map(Object::toString)
                                .orElse("")));
                        buffered.write(',');
                        buffered.write(csv(String.valueOf(
                                transaction.computeAmount(transaction.computeTo()))));
                        buffered.newLine();
                    }

                    buffered.flush();
                    outputStream.flush();
                    filter.page(++currentPage, 100);
                } while (transactions.hasNext());
            } catch (IOException e) {
                log.error("Failed to write transactions to CSV.", e);
                throw new RuntimeException(e);
            }
        };
    }

    private static String csv(String value) {
        if (value == null) return "";
        boolean mustQuote = value.indexOf(',') >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;
        if (!mustQuote) return value;
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private ExportProfileResponseRuleGroupsInner convertRuleGroup(TransactionRuleGroup ruleGroup) {
        var response =
                new ExportProfileResponseRuleGroupsInner(ruleGroup.getSort(), ruleGroup.getName());

        transactionRuleProvider
                .lookup(ruleGroup.getName())
                .map(RuleMapper::convertToRuleResponse)
                .forEach(response::addRulesItem);

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
        return new ExportProfileResponseBudgetInner(
                budget.getExpectedIncome(),
                new DateRange(budget.getStart(), budget.getEnd()),
                budget.getExpenses()
                        .map(expense -> new ExportProfileResponseBudgetInnerExpensesInner(
                                expense.getId(),
                                expense.getName(),
                                BigDecimal.valueOf(expense.computeBudget())))
                        .toJava());
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
