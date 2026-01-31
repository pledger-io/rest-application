package com.jongsoft.finance.contract.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jongsoft.finance.EventBus;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.contract.adapter.api.ContractProvider;
import com.jongsoft.finance.contract.domain.model.Contract;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.adapter.mail.MailDaemon;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.lang.Collections;

import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.reflect.ReflectionUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Properties;

@Tag("unit")
@DisplayName("Unit - Contract Expiration Schedule")
class ContractExpireScheduleTest {

    @Test
    void checkExpiredContracts() {
        ContractProvider contractProvider = mock(ContractProvider.class);
        UserProvider userProvider = mock(UserProvider.class);
        MailDaemon mailDaemon = mock(MailDaemon.class);

        new EventBus(mock(ApplicationEventPublisher.class));
        ContractExpireSchedule schedule =
                new ContractExpireSchedule(contractProvider, userProvider, mailDaemon);

        doReturn(Collections.List(UserAccount.create("account-1@account", "")))
                .when(userProvider)
                .lookup();

        doReturn(Collections.List(
                        createContract("Contract 0", false, false),
                        createContract("Contract 1", true, false),
                        createContract("Contract 2", true, true)))
                .when(contractProvider)
                .lookup();

        schedule.checkExpiredContracts();

        ArgumentCaptor<Properties> captor = ArgumentCaptor.forClass(Properties.class);
        verify(mailDaemon, times(1))
                .send(eq("account-1@account"), eq("contract-expiring"), captor.capture());

        Properties properties = captor.getValue();
        assertThat(properties.get("contract"))
                .isInstanceOf(Contract.class)
                .extracting("name")
                .isEqualTo("Contract 1");
    }

    private Contract createContract(String name, boolean warnBeforeExpire, boolean alreadySent) {
        Contract contract = Contract.create(
                mock(Account.class),
                name,
                "",
                LocalDate.now().minusMonths(2),
                LocalDate.now().plusMonths(1));

        ReflectionUtils.setField(Contract.class, "id", contract, 1L);

        if (warnBeforeExpire) {
            contract.warnBeforeExpires();
        }
        if (alreadySent) {
            contract.notificationSend();
        }
        return contract;
    }
}
