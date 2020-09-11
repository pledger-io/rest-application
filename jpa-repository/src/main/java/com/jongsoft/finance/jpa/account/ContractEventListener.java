package com.jongsoft.finance.jpa.account;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.account.ContractListener;
import com.jongsoft.finance.domain.account.events.ContractChangedEvent;
import com.jongsoft.finance.domain.account.events.ContractCreatedEvent;
import com.jongsoft.finance.domain.account.events.ContractTerminatedEvent;
import com.jongsoft.finance.domain.account.events.ContractUploadEvent;
import com.jongsoft.finance.domain.account.events.ContractWarningEvent;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.jpa.account.entity.AccountJpa;
import com.jongsoft.finance.jpa.account.entity.ContractJpa;

@Singleton
@Transactional
public class ContractEventListener implements ContractListener {

    private final Logger logger;
    private final EntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    public ContractEventListener(AuthenticationFacade authenticationFacade, EntityManager entityManager) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @BusinessEventListener
    public void handleContractCreated(ContractCreatedEvent event) {
        logger.trace("[{}] - Processing contract create event", event.getName());

        var company = entityManager.find(AccountJpa.class, event.getCompany().getId());
        var contract = ContractJpa.builder()
                .name(event.getName())
                .startDate(event.getStart())
                .endDate(event.getEnd())
                .description(event.getDescription())
                .company(company)
                .user(company.getUser())
                .build();

        entityManager.persist(contract);
    }

    @BusinessEventListener
    public void handleContractChanged(ContractChangedEvent event) {
        logger.trace("[{}] - Processing contract changed event", event.getId());

        var hql = """
                update ContractJpa
                set name = :name,
                    startDate = :startDate,
                    endDate = :endDate,
                    description = :description
                where id = :id""";
        var query = entityManager.createQuery(hql);
        query.setParameter("name", event.getName());
        query.setParameter("startDate", event.getStart());
        query.setParameter("endDate", event.getEnd());
        query.setParameter("description", event.getDescription());
        query.setParameter("id", event.getId());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleContractWarning(ContractWarningEvent event) {
        logger.trace("[{}] - Processing contract warning event", event.getContractId());

        var hql = """
                update ContractJpa
                set warningActive = true,
                    endDate = :endDate
                where id = :id""";
        var query = entityManager.createQuery(hql);
        query.setParameter("endDate", event.getEndDate());
        query.setParameter("id", event.getContractId());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleContractUpload(ContractUploadEvent event) {
        logger.trace("[{}] - Processing contract upload event", event.getId());

        var hql = """
                update ContractJpa
                set fileToken = :token
                where id = :id""";
        var query = entityManager.createQuery(hql);
        query.setParameter("token", event.getStorageToken());
        query.setParameter("id", event.getId());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleContractTerminated(ContractTerminatedEvent event) {
        logger.trace("[{}] - Processing contract terminate event", event.getId());

        var hql = """
                update ContractJpa c 
                set c.archived = true 
                where c.id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getId());
        query.executeUpdate();
    }

}
