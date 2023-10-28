package com.jongsoft.finance.rest.importer;

import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.security.Principal;
import java.time.LocalDate;

class ImporterTransactionResourceTest extends TestSetup {

    private ImporterTransactionResource subject;

    private FilterFactory filterFactory;
    @Mock private SettingProvider settingProvider;
    @Mock private TransactionProvider transactionProvider;
    @Mock private Principal principal;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        subject = new ImporterTransactionResource(settingProvider, filterFactory, transactionProvider);

        Mockito.when(principal.getName()).thenReturn(ACTIVE_USER.getUsername());
    }

    @Test
    void search() {
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
                                .build()));

        var response = subject.search("ads-fasdfa-fasd", new TransactionSearchRequest(0));
        var mockFilter = filterFactory.transaction();

        Assertions.assertThat(response.getContent().get(0).getDescription()).isEqualTo("Sample transaction");
        Mockito.verify(mockFilter).importSlug("ads-fasdfa-fasd");
        Mockito.verify(mockFilter).page(0);
        Mockito.verify(transactionProvider).lookup(Mockito.any());
    }

    @Test
    void delete() {
        Transaction transaction = Mockito.mock(Transaction.class);

        Mockito.when(transaction.getUser()).thenReturn(ACTIVE_USER);
        Mockito.when(transactionProvider.lookup(123L)).thenReturn(Control.Option(transaction));

        subject.delete(123L, principal);

        Mockito.verify(transactionProvider).lookup(123L);
        Mockito.verify(transaction).delete();
    }

}
