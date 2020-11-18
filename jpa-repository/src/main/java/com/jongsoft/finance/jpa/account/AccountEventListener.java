package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.account.AccountListener;
import com.jongsoft.finance.domain.account.events.*;
import com.jongsoft.finance.jpa.account.entity.AccountJpa;
import com.jongsoft.finance.jpa.account.entity.AccountSynonymJpa;
import com.jongsoft.finance.jpa.account.entity.AccountTypeJpa;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.core.entity.CurrencyJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.security.AuthenticationFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Singleton
@Transactional
public class AccountEventListener extends RepositoryJpa implements AccountListener {

    private final Logger logger;
    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    public AccountEventListener(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
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

        entityManager.update()
                .hql(hql)
                .set("name", event.getName())
                .set("description", event.getDescription())
                .set("type", locate(event.getType()))
                .set("currency", currency(event.getCurrency()))
                .set("id", event.getAccountId())
                .update();
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

        entityManager.update()
                .hql(hql)
                .set("iban", event.getIban())
                .set("bic", event.getBic())
                .set("number", event.getNumber())
                .set("id", event.getAccountId())
                .update();
    }

    @BusinessEventListener
    public void handleInterestChange(AccountInterestEvent event) {
        logger.trace("[{}] - Processing account interest event", event.getAccountId());

        var hql = """
                update AccountJpa 
                set interest = :interest,
                    interestPeriodicity = :periodicity
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", event.getAccountId())
                .set("interest", event.getInterest())
                .set("periodicity", event.getInterestPeriodicity())
                .update();
    }

    @BusinessEventListener
    public void handleAccountTerminate(AccountTerminatedEvent event) {
        logger.trace("[{}] - Processing account terminate event", event.getAccount().getId());

        entityManager.update()
                .hql("update AccountJpa a set a.archived = true where a.id = :id")
                .set("id", event.getAccount().getId())
                .update();
    }

    @BusinessEventListener
    public void handleRegisterSynonym(AccountSynonymEvent event) {
        logger.trace("[{}] - Processing register synonym event", event.getAccountId());

        var hql = """
                select id from AccountSynonymJpa where 
                    synonym = :synonym
                    and account.user.username = :username""";

        var existingId = entityManager.blocking()
                .hql(hql)
                .set("synonym", event.getSynonym())
                .set("username", authenticationFacade.authenticated())
                .maybe();

        if (existingId.isPresent()) {
            var updateHql = """
                    update AccountSynonymJpa 
                    set account = :account 
                    where id = :id""";

            entityManager.update()
                    .hql(updateHql)
                    .set("account", locate(event.getAccountId()))
                    .set("id", existingId.get())
                    .update();
        } else {
            var entity = AccountSynonymJpa.builder()
                    .account(locate(event.getAccountId()))
                    .synonym(event.getSynonym())
                    .build();

            entityManager.persist(entity);
        }
    }

    @BusinessEventListener
    public void handleIconRegisterEvent(AccountIconAttachedEvent event) {
        logger.trace("[{}] - Processing icon registration event", event.getAccountId());

        var hql = """
                update AccountJpa
                set imageFileToken = :fileCode
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("fileCode", event.getFileCode())
                .set("id", event.getAccountId())
                .update();
    }

    private AccountJpa locate(long id) {
        return entityManager.<AccountJpa>blocking()
                .hql("select a from AccountJpa a where a.id = :id")
                .set("id", id)
                .maybe()
                .get();
    }

    private AccountTypeJpa locate(String label) {
        return entityManager.<AccountTypeJpa>blocking()
                .hql("select l from AccountTypeJpa l where l.label = :label")
                .set("label", label)
                .maybe()
                .get();
    }

    private UserAccountJpa activeUser() {
        return entityManager.<UserAccountJpa>blocking()
                .hql("select u from UserAccountJpa u where u.username = :username")
                .set("username", authenticationFacade.authenticated())
                .maybe()
                .get();
    }

    private CurrencyJpa currency(String currency) {
        var hql = """
                select c from CurrencyJpa c 
                where c.code = :code""";
        return entityManager.<CurrencyJpa>blocking()
                .hql(hql)
                .set("code", currency)
                .maybe()
                .get();
    }

}
