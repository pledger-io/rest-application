package com.jongsoft.finance.jpa.reactive;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.jpa.ResultPageImpl;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import javax.persistence.EntityManager;
import java.util.List;

public class NonReactivePipe<T> extends JpaPipe<T, NonReactivePipe<T>> {

    private final EntityManager entityManager;

    public NonReactivePipe(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    public Optional<T> maybe() {
        var query = entityManager.createQuery(hql());

        applyParameters(query);
        applyPaging(query);

        return Control.Try(() -> (T) query.getSingleResult())
                .map(Control::Option)
                .recover(e -> Control.Option())
                .get();
    }

    @SuppressWarnings("unchecked")
    public Sequence<T> sequence() {
        var query = entityManager.createQuery(hql() + sort());

        applyParameters(query);
        applyPaging(query);

        return Collections.List(query.getResultList());
    }

    @SuppressWarnings("unchecked")
    public ResultPage<T> page() {
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
    }

    @Override
    protected NonReactivePipe<T> self() {
        return this;
    }
}
