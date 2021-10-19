package com.jongsoft.finance.jpa.reactive;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.lang.Control;
import io.micronaut.transaction.SynchronousTransactionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.persistence.EntityManager;
import java.sql.Connection;

@Slf4j
@RequiredArgsConstructor
public class ReactivePipe<T> extends JpaPipe<T, ReactivePipe<T>> {

    private final EntityManager entityManager;
    private final SynchronousTransactionManager<Connection> transactionManager;

    @SuppressWarnings("unchecked")
    public Mono<T> maybe() {
        return Mono.create(emitter -> transactionManager.executeRead(status -> {
            var query = entityManager.createQuery(hql());

            applyParameters(query);
            applyPaging(query);

            var result = Control.Try(() -> (T) query.getSingleResult())
                    .consume(emitter::success);

            if (result.isFailure()) {
                emitter.success();
            }
            return null;
        }));
    }

    @SuppressWarnings("unchecked")
    public Flux<T> flow() {
        if (log.isTraceEnabled()) {
            log.trace("Executing reactive query `{}`", hql().replaceAll("\n", " "));
        }

        return Flux.create(emitter -> transactionManager.executeRead(status -> {
            var query = entityManager.createQuery(hql());

            applyParameters(query);
            applyPaging(query);

            query.getResultStream()
                    .forEach(entity -> emitter.next((T) entity));

            emitter.complete();
            return null;
        }));
    }

    @SuppressWarnings("unchecked")
    public Mono<T> single() {
        return Mono.create(emitter -> transactionManager.executeRead(status -> {
            var query = entityManager.createQuery(hql());

            applyParameters(query);
            applyPaging(query);

            var result = Control.Try(() -> (T) query.getSingleResult());
            if (result.isSuccess()) {
                emitter.success(result.get());
            } else {
                emitter.error(StatusException.notFound("Entity not found"));
            }
            return null;
        }));
    }

    @Override
    protected ReactivePipe<T> self() {
        return this;
    }
}
