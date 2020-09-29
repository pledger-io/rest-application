package com.jongsoft.finance.jpa.reactive;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.lang.API;
import io.micronaut.transaction.SynchronousTransactionManager;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import javax.persistence.EntityManager;
import java.sql.Connection;

public class ReactivePipe<T> extends JpaPipe<T, ReactivePipe<T>> {

    private final EntityManager entityManager;
    private final SynchronousTransactionManager<Connection> transactionManager;

    public ReactivePipe(EntityManager entityManager, SynchronousTransactionManager<Connection> transactionManager) {
        this.entityManager = entityManager;
        this.transactionManager = transactionManager;
    }

    @SuppressWarnings("unchecked")
    public Maybe<T> maybe() {
        return Maybe.create(emitter -> {
            var query = entityManager.createQuery(hql());

            applyParameters(query);
            applyPaging(query);

            API.Try(() -> (T) query.getSingleResult())
                    .consume(emitter::onSuccess);

            emitter.onComplete();
        });
    }

    @SuppressWarnings("unchecked")
    public Flowable<T> flow() {
        return Flowable.create(emitter -> {
            transactionManager.executeRead(status -> {
                var query = entityManager.createQuery(hql());

                applyParameters(query);
                applyPaging(query);
                query.getResultStream()
                        .forEach(entity -> emitter.onNext((T) entity));

                emitter.onComplete();
                return null;
            });
        }, BackpressureStrategy.BUFFER);
    }

    @SuppressWarnings("unchecked")
    public Single<T> single() {
        return Single.create(emitter -> {
            transactionManager.executeRead(status -> {
                var query = entityManager.createQuery(hql());

                applyParameters(query);
                applyPaging(query);

                var result = API.Try(() -> (T) query.getSingleResult());
                if (result.isSuccess()) {
                    emitter.onSuccess(result.get());
                } else {
                    emitter.onError(StatusException.notFound("Entity not found"));
                }
                return null;
            });
        });
    }

    @Override
    protected ReactivePipe<T> self() {
        return this;
    }
}
