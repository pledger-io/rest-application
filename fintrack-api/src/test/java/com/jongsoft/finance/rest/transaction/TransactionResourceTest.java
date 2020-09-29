package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.account.AccountTypeProvider;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.rest.process.RuntimeResource;
import com.jongsoft.lang.API;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

class TransactionResourceTest extends TestSetup {

    private TransactionResource subject;

    @Mock
    private SettingProvider settingProvider;
    @Mock
    private TransactionProvider transactionProvider;
    @Mock
    private AccountProvider accountProvider;
    @Mock
    private AccountTypeProvider accountTypeProvider;
    @Mock
    private RuntimeResource runtimeResource;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private FilterFactory filterFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        filterFactory = generateFilterMock();

        subject = new TransactionResource(
                settingProvider,
                transactionProvider,
                accountProvider,
                filterFactory,
                accountTypeProvider,
                runtimeResource,
                eventPublisher);

        new EventBus(eventPublisher);
    }

    @Test
    void search() {
        Account account = Account.builder()
                .id(1L)
                .name("To account")
                .type("checking")
                .currency("EUR")
                .build();

        Mockito.when(transactionProvider.lookup(Mockito.any()))
                .thenReturn(ResultPage.of(
                        Transaction.builder()
                                .id(1L)
                                .description("Sample transaction")
                                .category("Grocery")
                                .currency("EUR")
                                .budget("Household")
                                .date(LocalDate.of(2019, 1, 15))
                                .transactions(API.List(
                                        Transaction.Part.builder()
                                                .id(1L)
                                                .account(account)
                                                .amount(20.00D)
                                                .build(),
                                        Transaction.Part.builder()
                                                .id(2L)
                                                .account(Account.builder()
                                                        .id(2L)
                                                        .currency("EUR")
                                                        .type("debtor")
                                                        .name("From account")
                                                        .build())
                                                .amount(-20.00D)
                                                .build()
                                ))
                                .build()
                ));

        var request = TransactionSearchRequest.builder()
                .page(0)
                .dateRange(DateRange.forMonth(2019, 1))
                .onlyIncome(false)
                .onlyExpenses(false)
                .description("samp")
                .build();

        subject.search(request);

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider).lookup(Mockito.any());
        Mockito.verify(mockFilter).description("samp", false);
        Mockito.verify(mockFilter).ownAccounts();
        Mockito.verify(mockFilter).range(DateRange.forMonth(2019, 1));
    }

    @Test
    void patch() {
        var transaction = Mockito.spy(Transaction.builder()
                .id(1L)
                .description("Sample transaction")
                .category("Grocery")
                .currency("EUR")
                .budget("Household")
                .user(ACTIVE_USER)
                .date(LocalDate.of(2019, 1, 15))
                .transactions(API.List(
                        Transaction.Part.builder()
                                .id(1L)
                                .account(Account.builder()
                                        .id(1L)
                                        .name("To account")
                                        .type("checking")
                                        .currency("EUR")
                                        .build())
                                .amount(20.00D)
                                .build(),
                        Transaction.Part.builder()
                                .id(2L)
                                .account(Account.builder().id(2L).currency("EUR").type("debtor").name("From account").build())
                                .amount(-20.00D)
                                .build()
                ))
                .build());

        var principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(ACTIVE_USER.getUsername());
        Mockito.when(transactionProvider.lookup(Mockito.anyLong())).thenReturn(API.Option());
        Mockito.when(transactionProvider.lookup(1L)).thenReturn(API.Option(transaction));

        var request = TransactionBulkEditRequest.builder()
                .transactions(List.of(1L, 2L))
                .budget(new TransactionBulkEditRequest.EntityRef(-1L, "Groceries"))
                .category(new TransactionBulkEditRequest.EntityRef(-1L, "Category"))
                .contract(new TransactionBulkEditRequest.EntityRef(-1L, "Wallmart"))
                .tags(List.of("sample"))
                .build();

        subject.patch(request, principal);

        Mockito.verify(transaction).linkToCategory("Category");
        Mockito.verify(transaction).linkToBudget("Groceries");
        Mockito.verify(transaction).linkToContract("Wallmart");
        Mockito.verify(transaction).tag(API.List("sample"));
    }

    @Test
    void firstTransaction() {
        Mockito.when(transactionProvider.first(Mockito.any())).thenReturn(API.Option(
                Transaction.builder()
                        .date(LocalDate.of(2019, 1, 1))
                        .build()));

        var request = TransactionSearchRequest.builder()
                .description("test")
                .build();

        var response = subject.firstTransaction(request);

        Assertions.assertThat(response).isEqualTo(LocalDate.of(2019, 1, 1));
    }

    @Test
    void export() {
        Account account = Account.builder()
                .id(1L)
                .name("To account")
                .type("checking")
                .currency("EUR")
                .build();

        Mockito.when(accountTypeProvider.lookup(false)).thenReturn(API.List());
        Mockito.when(accountProvider.lookup(Mockito.any(AccountProvider.FilterCommand.class)))
                .thenReturn(ResultPage.empty());
        Mockito.when(transactionProvider.lookup(Mockito.any()))
                .thenReturn(ResultPage.of(
                        Transaction.builder()
                                .id(1L)
                                .description("Sample transaction")
                                .category("Grocery")
                                .currency("EUR")
                                .budget("Household")
                                .date(LocalDate.of(2019, 1, 15))
                                .transactions(API.List(
                                        Transaction.Part.builder()
                                                .id(1L)
                                                .account(account)
                                                .amount(20.00D)
                                                .build(),
                                        Transaction.Part.builder()
                                                .id(2L)
                                                .account(Account.builder().id(2L).currency("EUR").type("debtor").name("From account").build())
                                                .amount(-20.00D)
                                                .build()
                                ))
                                .build()
                ))
                .thenReturn(ResultPage.empty());

        var response = subject.export().test().awaitCount(2);

        response.assertComplete();
        response.assertValueCount(2);
    }

}