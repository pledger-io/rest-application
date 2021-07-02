package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.providers.AccountTypeProvider;
import com.jongsoft.lang.collection.Sequence;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("accountTypeProvider")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AccountTypeProviderJpa implements AccountTypeProvider {

    public static final Logger LOGGER = LoggerFactory.getLogger(AccountTypeProvider.class);

    private final ReactiveEntityManager entityManager;

    @Override
    public Sequence<String> lookup(boolean hidden) {
        LOGGER.debug("Locating account types with hidden: {}", hidden);

        String hql = """
                select t from AccountTypeJpa t
                where t.hidden = :hidden
                order by t.label ASC""";

        return entityManager.<AccountTypeJpa>blocking()
                .hql(hql)
                .set("hidden", hidden)
                .sequence()
                .map(AccountTypeJpa::getLabel);
    }

}
