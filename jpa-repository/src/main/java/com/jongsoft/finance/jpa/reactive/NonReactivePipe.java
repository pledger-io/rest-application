package com.jongsoft.finance.jpa.reactive;

import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.jpa.ResultPageImpl;
import com.jongsoft.lang.API;
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

        return API.Try(() -> (T) query.getSingleResult())
                .map(API::Option)
                .recover(e -> API.Option())
                .get();
    }

    @SuppressWarnings("unchecked")
    public Sequence<T> sequence() {
        var query = entityManager.createQuery(hql());

        applyParameters(query);
        applyPaging(query);

        return API.List(query.getResultList());
    }

    @SuppressWarnings("unchecked")
    public ResultPage<T> page() {
        var countHql = "select count(distinct a.id) " + hql();
        var selectHql = "select distinct a " + hql() + sort();

        var countQuery = entityManager.createQuery(countHql, Long.class);
        var selectQuery = entityManager.createQuery(selectHql, Long.class);

        applyParameters(countQuery);
        applyParameters(selectQuery);
        applyPaging(selectQuery);

        long hits = countQuery.getSingleResult();

        return new ResultPageImpl<>(
                API.List((List<T>) selectQuery.getResultList()),
                limit(),
                hits);
    }

    @Override
    protected NonReactivePipe<T> self() {
        return this;
    }
}
