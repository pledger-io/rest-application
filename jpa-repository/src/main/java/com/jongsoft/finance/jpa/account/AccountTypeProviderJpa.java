package com.jongsoft.finance.jpa.account;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jongsoft.finance.domain.account.AccountTypeProvider;
import com.jongsoft.finance.jpa.account.entity.AccountTypeJpa;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;

@Singleton
@Transactional()
@Named("accountTypeProvider")
public class AccountTypeProviderJpa implements AccountTypeProvider {

    public static final Logger LOGGER = LoggerFactory.getLogger(AccountTypeProvider.class);

    private final EntityManager entityManager;

    public AccountTypeProviderJpa(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Sequence<String> lookup(boolean hidden) {
        LOGGER.debug("Locating account types with hidden: {}", hidden);

        String hql = """
                select t from AccountTypeJpa t
                where t.hidden = :hidden
                order by t.label ASC""";

        var query = entityManager.createQuery(hql);
        query.setParameter("hidden", hidden);

        return ((Sequence<AccountTypeJpa>) API.List(query.getResultList()))
                .map(AccountTypeJpa::getLabel);
    }

}
