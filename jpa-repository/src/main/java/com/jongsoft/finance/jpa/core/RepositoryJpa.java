package com.jongsoft.finance.jpa.core;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;

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
        return API.List(query.getResultList());
    }

}
