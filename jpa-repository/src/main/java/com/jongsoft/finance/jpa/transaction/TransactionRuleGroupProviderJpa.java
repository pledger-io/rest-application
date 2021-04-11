package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.domain.transaction.TransactionRuleGroup;
import com.jongsoft.finance.providers.TransactionRuleGroupProvider;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.transaction.entity.RuleGroupJpa;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.control.Optional;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
@Named("transactionRuleGroupProvider")
public class TransactionRuleGroupProviderJpa implements TransactionRuleGroupProvider {

    private final AuthenticationFacade authenticationFacade;
    private final ReactiveEntityManager entityManager;

    public TransactionRuleGroupProviderJpa(
            AuthenticationFacade authenticationFacade,
            ReactiveEntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
    }

    @Override
    public Flowable<TransactionRuleGroup> lookup() {
        log.trace("TransactionRuleGroup listing");

        var hql = """
                select g from RuleGroupJpa g 
                where g.user.username = :username
                    and g.archived = false
                order by g.sort asc""";

        return entityManager.<RuleGroupJpa>reactive()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .flow()
                .map(this::convert);
    }

    @Override
    public Optional<TransactionRuleGroup> lookup(String name) {
        log.trace("TransactionRuleGroup lookup with name: {}", name);

        var hql = """
                select g from RuleGroupJpa g 
                where g.user.username = :username
                    and g.name = :name
                    and g.archived = false""";

        return entityManager.<RuleGroupJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("name", name)
                .maybe()
                .map(this::convert);
    }

    private TransactionRuleGroup convert(RuleGroupJpa source) {
        if (source == null) {
            return null;
        }

        return TransactionRuleGroup.builder()
                .id(source.getId())
                .name(source.getName())
                .sort(source.getSort())
                .build();
    }

}
