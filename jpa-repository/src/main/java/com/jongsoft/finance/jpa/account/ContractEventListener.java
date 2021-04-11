package com.jongsoft.finance.jpa.account;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.messaging.handlers.ContractListener;
import com.jongsoft.finance.domain.account.events.ContractUploadEvent;
import com.jongsoft.finance.domain.account.events.ContractWarningEvent;
import com.jongsoft.finance.security.AuthenticationFacade;

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

}
