package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.*;
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
    private final FilterFactory filterFactory;

    private final StorageService storageService;

    public ExportController(
            AccountProvider accountProvider,
            CategoryProvider categoryProvider,
            ContractProvider contractProvider,
            BudgetProvider budgetProvider,
            TagProvider tagProvider,
            TransactionProvider transactionProvider,
            FilterFactory filterFactory,
            StorageService storageService) {
        this.accountProvider = accountProvider;
        this.categoryProvider = categoryProvider;
        this.contractProvider = contractProvider;
        this.budgetProvider = budgetProvider;
        this.tagProvider = tagProvider;
        this.transactionProvider = transactionProvider;
        this.filterFactory = filterFactory;
        this.storageService = storageService;
    }

    @Override
    public ExportProfileResponse exportUserAccount() {
        var response = new ExportProfileResponse();

        // todo convert Rules

        response.accounts(accountProvider.lookup().map(this::toAccountResponse).toJava());
        response.setCategories(categoryProvider
                .lookup()
                .map(CategoryMapper::toCategoryResponse)
                .toJava());
        response.setContract(
                contractProvider.lookup().map(this::toContractResponse).toJava());
        response.setBudget(budgetProvider.lookup().map(this::toBudgetResponse).toJava());
        response.setTags(tagProvider.lookup().map(Tag::name).toJava());
        response.setTransaction(lookupRelevantTransactions());

        return response;
    }

    private List<TransactionResponse> lookupRelevantTransactions() {
        // we also want to export all opening balance transactions for liability accounts
        var filter = filterFactory
                .transaction()
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

        response.period(new DateRange(budget.getStart(), budget.getEnd()));
        for (var expense : budget.getExpenses()) {
            var responseExpense = new ExportProfileResponseBudgetInnerExpensesInner();
            responseExpense.name(expense.getName());
            responseExpense.expected(new BigDecimal(expense.computeBudget()));
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
