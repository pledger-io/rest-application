package com.jongsoft.finance.jpa.reactive;

import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

import javax.persistence.EntityManager;

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

    @Override
    protected NonReactivePipe<T> self() {
        return this;
    }
}
