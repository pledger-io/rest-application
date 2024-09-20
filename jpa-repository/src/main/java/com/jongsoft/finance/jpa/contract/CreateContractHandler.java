package com.jongsoft.finance.jpa.contract;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.contract.CreateContractCommand;
import com.jongsoft.lang.Collections;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class CreateContractHandler implements CommandHandler<CreateContractCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public CreateContractHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(CreateContractCommand command) {
        log.info("[{}] - Processing contract create event", command.name());

        var company = entityManager.get(AccountJpa.class, Collections.Map("id", command.companyId()));

        var contract = ContractJpa.builder()
                .name(command.name())
                .startDate(command.start())
                .endDate(command.end())
                .description(command.description())
                .company(company)
                .user(company.getUser())
                .build();

        entityManager.persist(contract);
    }

}
