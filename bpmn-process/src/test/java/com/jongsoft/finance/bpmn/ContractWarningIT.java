package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.core.MailDaemon;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.providers.ContractProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Properties;

public class ContractWarningIT extends ProcessTestSetup {

    @Inject
    private ProcessEngine processEngine;

    @Inject
    private CurrentUserProvider currentUserProvider;

    @Inject
    private ContractProvider contractProvider;

    @Inject
    private MailDaemon mailDaemon;

    @BeforeEach
    void setup() {
        Mockito.reset(currentUserProvider, mailDaemon, contractProvider);

        Mockito.when(currentUserProvider.currentUser()).thenReturn(
                UserAccount.builder()
                        .id(1L)
                        .username("test-user")
                        .password("12345")
                        .roles(Collections.List(new Role("admin")))
                        .build());
    }

    @Test
    @DisplayName("Contract is about to expire.")
    void runContract() throws InterruptedException {
        final Contract contract = Contract.builder()
                .id(1L)
                .name("Sample contract")
                .startDate(LocalDate.now().minusMonths(12))
                .endDate(LocalDate.now().plusMonths(1))
                .terminated(false)
                .company(Account.builder().build())
                .notifyBeforeEnd(false)
                .build();

        Mockito.when(contractProvider.lookup(1L)).thenReturn(Control.Option(contract));

        var process = processEngine.getRuntimeService().createProcessInstanceByKey("ContractEndWarning")
                .setVariable("username", currentUserProvider.currentUser().getUsername())
                .setVariable("contractId", contract.getId())
                .setVariable("warnAt", convert(LocalDate.now().plusDays(1)))
                .execute();

        waitForSuspended(processEngine, process.getProcessInstanceId());

        var jobs = processEngine.getManagementService()
                .createJobQuery()
                .processInstanceId(process.getProcessInstanceId())
                .singleResult();

        processEngine.getManagementService()
                .executeJob(jobs.getId());

        waitForSuspended(processEngine, process.getProcessInstanceId());

        ArgumentCaptor<Properties> propertiesCaptor = ArgumentCaptor.forClass(Properties.class);
        Mockito.verify(mailDaemon).send(Mockito.eq("test-user"), Mockito.eq("contract-expiring"), propertiesCaptor.capture());
        Mockito.verify(contractProvider).lookup(1L);

        Assertions.assertThat(propertiesCaptor.getValue()).containsKey("contract");
        Assertions.assertThat(propertiesCaptor.getValue().get("contract")).isEqualTo(contract);
        Assertions.assertThat(propertiesCaptor.getValue()).containsKey("username");
        Assertions.assertThat(propertiesCaptor.getValue().get("username")).isEqualTo("test-user");
    }

    @Test
    @DisplayName("Contract has already expired.")
    void run_InPast() {
        final Contract contract = Contract.builder()
                .id(1L)
                .name("Sample contract")
                .startDate(LocalDate.now().minusMonths(12))
                .endDate(LocalDate.now().minusDays(1))
                .terminated(false)
                .company(Account.builder().build())
                .notifyBeforeEnd(false)
                .build();

        var process = processEngine.getRuntimeService().createProcessInstanceByKey("ContractEndWarning")
                .setVariable("username", currentUserProvider.currentUser().getUsername())
                .setVariable("contractId", contract.getId())
                .setVariable("warnAt", convert(LocalDate.now().minusDays(1)))
                .execute();

        waitForSuspended(processEngine, process.getProcessInstanceId());

        Mockito.verify(mailDaemon, Mockito.never()).send(Mockito.eq("test-user"), Mockito.eq("contract-expiring"), Mockito.any());
    }

    public Date convert(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

}
