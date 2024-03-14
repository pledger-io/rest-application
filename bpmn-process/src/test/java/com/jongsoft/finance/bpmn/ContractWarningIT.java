package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.bpmn.process.ProcessExtension;
import com.jongsoft.finance.bpmn.process.RuntimeContext;
import com.jongsoft.finance.core.MailDaemon;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

@MicronautTest
@ProcessExtension
@DisplayName("Contract warning feature")
public class ContractWarningIT {

    @Test
    @DisplayName("Contract is about to expire.")
    void runContract(RuntimeContext context) {
        final Contract contract = createContract(LocalDate.now().plusMonths(1));

        context.withContract(contract);

        context.execute("ContractEndWarning", Map.of(
                        "contractId", contract.getId(),
                        "warnAt", convert(LocalDate.now().plusDays(1))
                ))
                .forceJob("timer_activate")
                .verifyCompleted();

        ArgumentCaptor<Properties> propertiesCaptor = ArgumentCaptor.forClass(Properties.class);
        context.verifyInteraction(MailDaemon.class)
                .send(Mockito.eq("test-user"), Mockito.eq("contract-expiring"), propertiesCaptor.capture());

        Assertions.assertThat(propertiesCaptor.getValue()).containsKey("contract");
        Assertions.assertThat(propertiesCaptor.getValue().get("contract")).isEqualTo(contract);
        Assertions.assertThat(propertiesCaptor.getValue()).containsKey("username");
        Assertions.assertThat(propertiesCaptor.getValue().get("username")).isEqualTo("test-user");
    }

    @Test
    @DisplayName("Contract has already expired.")
    void run_InPast(RuntimeContext context) {
        final Contract contract = createContract(LocalDate.now().minusDays(1));

        context.withContract(contract);

        context.execute("ContractEndWarning", Map.of(
                        "contractId", contract.getId(),
                        "warnAt", convert(LocalDate.now().minusDays(1))
                ))
                .verifyCompleted();
    }

    private static Contract createContract(LocalDate endDate) {
        return Contract.builder()
                .id(1L)
                .name("Sample contract")
                .startDate(LocalDate.now().minusMonths(12))
                .endDate(endDate)
                .terminated(false)
                .company(Account.builder().build())
                .notifyBeforeEnd(false)
                .build();
    }

    public Date convert(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

}
