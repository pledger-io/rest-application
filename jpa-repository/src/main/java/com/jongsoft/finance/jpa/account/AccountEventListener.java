package com.jongsoft.finance.jpa.account;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.account.AccountListener;
import com.jongsoft.finance.domain.account.events.AccountChangedEvent;
import com.jongsoft.finance.domain.account.events.AccountCreatedEvent;
import com.jongsoft.finance.domain.account.events.AccountInterestEvent;
import com.jongsoft.finance.domain.account.events.AccountRenamedEvent;
import com.jongsoft.finance.domain.account.events.AccountSynonymEvent;
import com.jongsoft.finance.domain.account.events.AccountTerminatedEvent;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.jpa.account.entity.AccountJpa;
import com.jongsoft.finance.jpa.account.entity.AccountSynonymJpa;
import com.jongsoft.finance.jpa.account.entity.AccountTypeJpa;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.core.entity.CurrencyJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

@Singleton
@Transactional
public class AccountEventListener extends RepositoryJpa implements AccountListener {

    private final Logger logger;
    private final EntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    public AccountEventListener(EntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @BusinessEventListener
    public void handleAccountCreate(AccountCreatedEvent event) {
        logger.trace("[{}] - Processing account create event", event.getName());

        var toCreate = AccountJpa.builder()
                .name(event.getName())
                .currency(currency(event.getCurrency()))
                .type(locate(event.getType()))
                .user(activeUser())
                .build();

        entityManager.persist(toCreate);
    }

    @BusinessEventListener
    public void handleAccountRename(AccountRenamedEvent event) {
        logger.trace("[{}] - Processing account rename event", event.getAccountId());

        var hql = """
                update AccountJpa
                set name = :name,
                    description = :description,
                    type = :type,
                    currency = :currency
                where id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("name", event.getName());
        query.setParameter("description", event.getDescription());
        query.setParameter("type", locate(event.getType()));
        query.setParameter("currency", currency(event.getCurrency()));
        query.setParameter("id", event.getAccountId());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleAccountChange(AccountChangedEvent event) {
        logger.trace("[{}] - Processing account change event", event.getAccountId());

        var hql = """
                update AccountJpa
                set iban = :iban,
                    bic = :bic,
                    number = :number
                where id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("iban", event.getIban());
        query.setParameter("bic", event.getBic());
        query.setParameter("number", event.getNumber());
        query.setParameter("id", event.getAccountId());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleInterestChange(AccountInterestEvent event) {
        logger.trace("[{}] - Processing account interest event", event.getAccountId());

        var hql = """
                update AccountJpa 
                set interest = :interest,
                    interestPeriodicity = :periodicity
                where id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getAccountId());
        query.setParameter("interest", event.getInterest());
        query.setParameter("periodicity", event.getInterestPeriodicity());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleAccountTerminate(AccountTerminatedEvent event) {
        logger.trace("[{}] - Processing account terminate event", event.getAccount().getId());
        var query = entityManager.createQuery("update AccountJpa a set a.archived = true where a.id = :id");
        query.setParameter("id", event.getAccount().getId());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleRegisterSynonym(AccountSynonymEvent event) {
        logger.trace("[{}] - Processing register synonym event", event.getAccountId());

        var existing = entityManager.createQuery("select id from AccountSynonymJpa where synonym = :synonym"
                + " and account.user.username = :username");
        existing.setParameter("synonym", event.getSynonym());
        existing.setParameter("username", authenticationFacade.authenticated());

        var id = existing.<Long>getSingleResult();
        if (id != null) {
            var hql = """
                    update AccountSynonymJpa 
                    set account = :account 
                    where id = :id""";
            var query = entityManager.createQuery(hql);
            query.setParameter("account", locate(event.getAccountId()));
            query.setParameter("id", id);
            query.executeUpdate();
        } else {
            var entity = AccountSynonymJpa.builder()
                    .account(locate(event.getAccountId()))
                    .synonym(event.getSynonym())
                    .build();

            entityManager.persist(entity);
        }
    }

    private AccountJpa locate(long id) {
        var query = entityManager.createQuery("select a from AccountJpa a where a.id = :id");
        query.setParameter("id", id);
        return singleValue(query);
    }

    private AccountTypeJpa locate(String label) {
        var query = entityManager.createQuery("select l from AccountTypeJpa l where l.label = :label");
        query.setParameter("label", label);
        return singleValue(query);
    }

    private UserAccountJpa activeUser() {
        var query = entityManager.createQuery("select u from UserAccountJpa u where u.username = :username");
        query.setParameter("username", authenticationFacade.authenticated());
        return singleValue(query);
    }

    private CurrencyJpa currency(String currency) {
        var hql = """
                select c from CurrencyJpa c 
                where c.code = :code""";
        var query = entityManager.createQuery(hql);
        query.setParameter("code", currency);
        return singleValue(query);
    }

}
