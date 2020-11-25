package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.http.HttpStatus;
import io.reactivex.Maybe;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

class AccountTransactionResourceTest extends TestSetup {

    private AccountTransactionResource subject;

    private SettingProvider settingProvider;
    private CurrentUserProvider currentUserProvider;
    private AccountProvider accountProvider;
    private FilterFactory filterFactory;
    private TransactionProvider transactionProvider;

    @BeforeEach
    void setup() {
        accountProvider = Mockito.mock(AccountProvider.class);
        currentUserProvider = Mockito.mock(CurrentUserProvider.class);
        filterFactory = generateFilterMock();
        transactionProvider = Mockito.mock(TransactionProvider.class);
        settingProvider = Mockito.mock(SettingProvider.class);

        subject = new AccountTransactionResource(filterFactory, transactionProvider, accountProvider, settingProvider);

        Mockito.when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);

        var applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);
    }

    @Test
    void search() {
        Account account = Account.builder()
                .id(1L)
                .name("To account")
                .type("checking")
                .user(ACTIVE_USER)
                .currency("EUR")
                .build();

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));

        Mockito.when(transactionProvider.lookup(Mockito.any()))
                .thenReturn(ResultPage.of(
                        Transaction.builder()
                                .id(1L)
                                .description("Sample transaction")
                                .category("Grocery")
                                .currency("EUR")
                                .budget("Household")
                                .date(LocalDate.of(2019, 1, 15))
                                .transactions(Collections.List(
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

        var request = AccountTransactionSearchRequest.builder()
                .build();

        var response = subject.search(1L, request).blockingGet();

        Assertions.assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());

        Mockito.verify(accountProvider).lookup(1L);
        Mockito.verify(transactionProvider).lookup(Mockito.any());
    }

    @Test
    void search_notfound() {
        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option());

        var response = subject.search(1L, new AccountTransactionSearchRequest()).blockingGet();

        Assertions.assertThat(response.code()).isEqualTo(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    void create() {
        final Account myAccount = Mockito.spy(Account.builder().id(1L).currency("EUR").type("checking").name("My account").build());
        final Account toAccount = Account.builder().id(2L).currency("EUR").type("creditor").name("Target account").build();

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(myAccount));
        Mockito.when(accountProvider.lookup(2L)).thenReturn(Control.Option(toAccount));

        var request = AccountTransactionCreateRequest.builder()
                .date(LocalDate.of(2019, 1, 1))
                .currency("EUR")
                .description("Sample transaction")
                .amount(20.2)
                .source(new AccountTransactionCreateRequest.EntityRef(1L, null))
                .destination(new AccountTransactionCreateRequest.EntityRef(2L, null))
                .category(new AccountTransactionCreateRequest.EntityRef(1L, null))
                .budget(new AccountTransactionCreateRequest.EntityRef(3L, null))
                .build();

        var response = subject.create(request).blockingGet();
        Assertions.assertThat(response.code()).isEqualTo(HttpStatus.CREATED.getCode());

        Mockito.verify(accountProvider).lookup(1L);
        Mockito.verify(accountProvider).lookup(2L);
        Mockito.verify(myAccount).createTransaction(
                Mockito.eq(toAccount),
                Mockito.eq(20.2D),
                Mockito.eq(Transaction.Type.CREDIT),
                Mockito.any());
    }

    @Test
    void first() {
        Account account = Account.builder()
                .id(1L)
                .name("To account")
                .type("checking")
                .user(ACTIVE_USER)
                .currency("EUR")
                .build();
        Transaction transaction = Transaction.builder()
                .id(1L)
                .description("Sample transaction")
                .category("Grocery")
                .user(ACTIVE_USER)
                .currency("EUR")
                .budget("Household")
                .date(LocalDate.of(2019, 1, 15))
                .transactions(Collections.List(
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
                .build();

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));
        Mockito.when(transactionProvider.first(Mockito.any(TransactionProvider.FilterCommand.class)))
                .thenReturn(Maybe.just(transaction));

        var response = subject.first(1L, null).blockingGet();

        Assertions.assertThat(response.getId()).isEqualTo(1L);
        Assertions.assertThat(response.getAmount()).isEqualTo(20D);
    }

    @Test
    void get() {
        Account account = Account.builder()
                .id(1L)
                .name("To account")
                .type("checking")
                .user(ACTIVE_USER)
                .currency("EUR")
                .build();
        Transaction transaction = Transaction.builder()
                .id(1L)
                .description("Sample transaction")
                .category("Grocery")
                .user(ACTIVE_USER)
                .currency("EUR")
                .budget("Household")
                .date(LocalDate.of(2019, 1, 15))
                .transactions(Collections.List(
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
                .build();

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));
        Mockito.when(transactionProvider.lookup(123L)).thenReturn(Control.Option(transaction));

        var response = subject.get(123L).blockingGet();

        Assertions.assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());

        var content = response.body();
        Assertions.assertThat(content.getId()).isEqualTo(1L);
        Assertions.assertThat(content.getAmount()).isEqualTo(20D);
        Assertions.assertThat(content.getDescription()).isEqualTo("Sample transaction");
        Assertions.assertThat(content.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void update() {
        Account account = Account.builder()
                .id(1L)
                .name("To account")
                .type("checking")
                .currency("EUR")
                .build();
        final Account toAccount = Account.builder()
                .id(2L)
                .currency("EUR")
                .type("debtor")
                .name("From account").build();

        Transaction transaction = Mockito.spy(Transaction.builder()
                .id(1L)
                .description("Sample transaction")
                .category("Grocery")
                .currency("EUR")
                .budget("Household")
                .date(LocalDate.of(2019, 1, 15))
                .user(ACTIVE_USER)
                .transactions(Collections.List(
                        Transaction.Part.builder()
                                .id(1L)
                                .account(account)
                                .amount(20.00D)
                                .build(),
                        Transaction.Part.builder()
                                .id(2L)
                                .account(toAccount)
                                .amount(-20.00D)
                                .build()
                ))
                .build());

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));
        Mockito.when(accountProvider.lookup(2L)).thenReturn(Control.Option(toAccount));
        Mockito.when(transactionProvider.lookup(123L)).thenReturn(Control.Option(transaction));

        var request = AccountTransactionCreateRequest.builder()
                .date(LocalDate.of(2019, 1, 1))
                .currency("EUR")
                .description("Updated transaction")
                .amount(20.2)
                .source(new AccountTransactionCreateRequest.EntityRef(1L, null))
                .destination(new AccountTransactionCreateRequest.EntityRef(2L, null))
                .category(new AccountTransactionCreateRequest.EntityRef(1L, null))
                .budget(new AccountTransactionCreateRequest.EntityRef(3L, null))
                .build();

        subject.update(123L, request).blockingGet();

        Mockito.verify(transaction).changeAmount(20.2D, "EUR");
        Mockito.verify(transaction).changeAccount(true, account);
        Mockito.verify(transaction).changeAccount(false, toAccount);
    }

    @Test
    void split() {
        Account account = Account.builder()
                .id(1L)
                .user(ACTIVE_USER)
                .name("To account")
                .type("checking")
                .currency("EUR")
                .build();
        final Account toAccount = Account.builder()
                .id(2L)
                .user(ACTIVE_USER)
                .currency("EUR")
                .type("debtor")
                .name("From account").build();

        Transaction transaction = Mockito.spy(Transaction.builder()
                .id(1L)
                .description("Sample transaction")
                .category("Grocery")
                .user(ACTIVE_USER)
                .currency("EUR")
                .budget("Household")
                .date(LocalDate.of(2019, 1, 15))
                .transactions(Collections.List(
                        Transaction.Part.builder()
                                .id(1L)
                                .account(account)
                                .amount(20.00D)
                                .build(),
                        Transaction.Part.builder()
                                .id(2L)
                                .account(toAccount)
                                .amount(-20.00D)
                                .build()
                ))
                .build());

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));
        Mockito.when(transactionProvider.lookup(123L)).thenReturn(Control.Option(transaction));

        var request = AccountTransactionSplitRequest.builder()
                .splits(List.of(
                        AccountTransactionSplitRequest.SplitRecord.builder()
                                .description("Part 1")
                                .amount(-5D)
                                .build(),
                        AccountTransactionSplitRequest.SplitRecord.builder()
                                .description("Part 2")
                                .amount(-15D)
                                .build()
                ))
                .build();

        var response = subject.split(123L, request).blockingGet();

        Assertions.assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());

        var check = response.body();
        Assertions.assertThat(check.getSplit().get(0).getAmount()).isEqualTo(5D);
        Assertions.assertThat(check.getSplit().get(0).getDescription()).isEqualTo("Part 1");
        Assertions.assertThat(check.getSplit().get(1).getAmount()).isEqualTo(15D);
        Assertions.assertThat(check.getSplit().get(1).getDescription()).isEqualTo("Part 2");
    }

    @Test
    void delete() {
        Transaction transaction = Mockito.mock(Transaction.class);
        Mockito.when(transaction.getUser()).thenReturn(ACTIVE_USER);

        Account account = Account.builder()
                .id(1L)
                .name("To account")
                .type("checking")
                .user(ACTIVE_USER)
                .currency("EUR")
                .build();

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));
        Mockito.when(transactionProvider.lookup(123L)).thenReturn(Control.Option(transaction));

        subject.delete(123L).blockingGet();

        Mockito.verify(transaction).delete();
    }

}
