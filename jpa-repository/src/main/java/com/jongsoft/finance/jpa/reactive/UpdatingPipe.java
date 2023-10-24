package com.jongsoft.finance.jpa.reactive;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdatingPipe extends JpaPipe<Void, UpdatingPipe> {

    private final EntityManager entityManager;

    @Transactional
    public void execute() {
        var query = entityManager.createQuery(hql());

        applyParameters(query);

        query.executeUpdate();
    }

    @Override
    protected UpdatingPipe self() {
        return this;
    }
}
