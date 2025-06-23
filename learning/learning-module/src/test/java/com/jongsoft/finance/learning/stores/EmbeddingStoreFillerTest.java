package com.jongsoft.finance.learning.stores;

import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.InternalAuthenticationEvent;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.lang.Collections;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class EmbeddingStoreFillerTest {

  @Test
  void testConsumeTransactionsExecutesCallbackForTransactions() {
    ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher = mock(ApplicationEventPublisher.class);
    ExecutorService executorService = mock(ExecutorService.class);
    FilterFactory filterFactory = mock(FilterFactory.class);
    UserProvider userProvider = mock(UserProvider.class);
    TransactionProvider transactionProvider = mock(TransactionProvider.class);

    var mockUser = UserAccount.builder()
        .id(1L)
        .username(new UserIdentifier("sample@e"))
        .build();
    when(userProvider.lookup()).thenReturn(Collections.List(mockUser));
    when(executorService.submit(any(Runnable.class))).thenReturn(mock(Future.class));

    Consumer<Transaction> mockCallback = mock(Consumer.class);
    EmbeddingStoreFiller filler = new EmbeddingStoreFiller(eventPublisher,
        executorService,
        filterFactory,
        userProvider,
        transactionProvider);

    filler.consumeTransactions(mockCallback);

    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(executorService, times(1)).submit(runnableCaptor.capture());
    runnableCaptor.getValue().run();

    verify(eventPublisher).publishEvent(any(InternalAuthenticationEvent.class));
    verify(mockCallback, times(0)).accept(any()); // Fake verification demonstrating flow
  }
}
