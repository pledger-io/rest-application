package com.jongsoft.finance.jpa.reactive;

import javax.persistence.EntityManager;

public class UpdatingPipe extends JpaPipe<Void, UpdatingPipe> {

    private final EntityManager entityManager;

    public UpdatingPipe(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void update() {
        var query = entityManager.createQuery(hql());

        applyParameters(query);

        query.executeUpdate();
    }

    @Override
    protected UpdatingPipe self() {
        return this;
    }
}
