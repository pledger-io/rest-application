package com.jongsoft.finance.learning.stores;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.learning.config.LearningExecutor;
import com.jongsoft.finance.messaging.InternalAuthenticationEvent;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.providers.UserProvider;

import io.micronaut.context.event.ApplicationEventPublisher;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@Singleton
public class EmbeddingStoreFiller {

    private final Logger logger = LoggerFactory.getLogger(EmbeddingStoreFiller.class);

    private final ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher;

    private final ExecutorService executorService;
    private final FilterFactory filterFactory;
    private final UserProvider userProvider;
    private final TransactionProvider transactionProvider;
    private final List<Future<?>> futures = new ArrayList<>();

    EmbeddingStoreFiller(
            ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher,
            @LearningExecutor ExecutorService executorService,
            FilterFactory filterFactory,
            UserProvider userProvider,
            TransactionProvider transactionProvider) {
        this.eventPublisher = eventPublisher;
        this.executorService = executorService;
        this.filterFactory = filterFactory;
        this.userProvider = userProvider;
        this.transactionProvider = transactionProvider;
    }

    public void consumeTransactions(Consumer<Transaction> callback) {
        for (var user : userProvider.lookup()) {
            futures.add(
                    executorService.submit(
                            () -> performInitialFill(user.getUsername().email(), callback)));
        }
    }

    public boolean isDone() {
        return futures.stream().allMatch(Future::isDone);
    }

    private void performInitialFill(String userId, Consumer<Transaction> callback) {
        logger.debug("Indexing transactions for user {}.", userId);
        eventPublisher.publishEvent(new InternalAuthenticationEvent(this, userId));

        try {
            var processingPage = 0;
            var filterApplied = filterFactory.transaction().ownAccounts().page(processingPage, 500);
            ResultPage<Transaction> transactionPage;
            do {
                transactionPage = transactionProvider.lookup(filterApplied);
                transactionPage.content().forEach(callback);
                filterApplied.page(++processingPage, 500);
                logger.trace(
                        "Processed page {} of transactions for user {}.", processingPage, userId);
            } while (transactionPage.hasNext());
        } catch (Exception e) {
            logger.error("Error indexing transactions for user {}.", userId, e);
        }
        logger.debug("Finished indexing transactions for user {}.", userId);
    }
}
