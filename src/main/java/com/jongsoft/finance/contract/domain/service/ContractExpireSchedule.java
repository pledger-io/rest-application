package com.jongsoft.finance.contract.domain.service;

import com.jongsoft.finance.contract.adapter.api.ContractProvider;
import com.jongsoft.finance.contract.domain.model.Contract;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.adapter.mail.MailDaemon;
import com.jongsoft.finance.core.domain.commands.InternalAuthenticationEvent;
import com.jongsoft.finance.core.domain.model.UserAccount;

import io.micronaut.scheduling.annotation.Scheduled;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Predicate;

@Singleton
class ContractExpireSchedule {

    private final Logger log = LoggerFactory.getLogger(ContractExpireSchedule.class);

    private final ContractProvider contractProvider;
    private final UserProvider userProvider;
    private final MailDaemon mailDaemon;

    ContractExpireSchedule(
            ContractProvider contractProvider, UserProvider userProvider, MailDaemon mailDaemon) {
        this.contractProvider = contractProvider;
        this.userProvider = userProvider;
        this.mailDaemon = mailDaemon;
    }

    @Scheduled(cron = "${application.schedules.contract.expired.cron}")
    void checkExpiredContracts() {
        LocalDate cutOfDate = LocalDate.now().plusMonths(2);
        for (UserAccount userAccount : userProvider.lookup()) {
            MDC.put("correlationId", UUID.randomUUID().toString());
            log.info(
                    "Checking for {} if there are expired contracts.",
                    userAccount.getUsername().email());
            InternalAuthenticationEvent.authenticate(userAccount.getUsername().email());

            var contracts = contractProvider
                    .lookup()
                    .filter(Predicate.not(Contract::isTerminated)
                            .and(Contract::isNotifyBeforeEnd)
                            .and(Predicate.not(Contract::isNotificationSend)))
                    .filter(contract -> contract.getEndDate().isBefore(cutOfDate));

            for (Contract contract : contracts) {
                sendWarningEmail(userAccount.getUsername().email(), contract);
                contract.notificationSend();
            }
        }
    }

    private void sendWarningEmail(String username, Contract contract) {
        Properties mailProperties = new Properties();
        mailProperties.put("username", username);
        mailProperties.put("contract", contract);
        mailDaemon.send(username, "contract-expiring", mailProperties);
    }
}
