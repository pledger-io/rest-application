package com.jongsoft.finance.jpa.core;

import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.Sequence;

import javax.persistence.NoResultException;
import javax.persistence.Query;

public abstract class RepositoryJpa {

    @SuppressWarnings("unchecked")
    protected <X> X singleValue(Query query) {
        try {
            return (X) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected <X> Sequence<X> multiValue(Query query) {
        return Collections.List(query.getResultList());
    }

}
