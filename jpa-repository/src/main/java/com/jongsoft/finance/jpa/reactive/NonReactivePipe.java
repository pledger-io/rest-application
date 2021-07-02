package com.jongsoft.finance.jpa.reactive;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.jpa.ResultPageImpl;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.SynchronousTransactionManager;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import java.sql.Connection;
import java.util.List;

@RequiredArgsConstructor
public class NonReactivePipe<T> extends JpaPipe<T, NonReactivePipe<T>> {

    private final EntityManager entityManager;
    private final SynchronousTransactionManager<Connection> transactionManager;

    @SuppressWarnings("unchecked")
    public Optional<T> maybe() {
        return transactionManager.executeRead(status -> {
            var query = entityManager.createQuery(hql());

            applyParameters(query);
            applyPaging(query);

            return Control.Try(() -> (T) query.getSingleResult())
                    .map(Control::Option)
                    .recover(e -> Control.Option())
                    .get();
        });
    }

    @SuppressWarnings("unchecked")
    public Sequence<T> sequence() {
        return transactionManager.executeRead(status -> {
            var query = entityManager.createQuery(hql() + sort());

            applyParameters(query);
            applyPaging(query);

            return Collections.List(query.getResultList());
        });
    }

    @SuppressWarnings("unchecked")
    public ResultPage<T> page() {
        return transactionManager.executeRead(status -> {
            var countHql = "select count(distinct a.id) " + hql();
            var selectHql = "select distinct a " + hql() + sort();

            var countQuery = entityManager.createQuery(countHql, Long.class);
            var selectQuery = entityManager.createQuery(selectHql);

            applyParameters(countQuery);
            applyParameters(selectQuery);
            applyPaging(selectQuery);

            long hits = countQuery.getSingleResult();

            return new ResultPageImpl<>(
                    Collections.List((List<T>) selectQuery.getResultList()),
                    limit(),
                    hits);
        });
    }

    @Override
    protected NonReactivePipe<T> self() {
        return this;
    }
}
