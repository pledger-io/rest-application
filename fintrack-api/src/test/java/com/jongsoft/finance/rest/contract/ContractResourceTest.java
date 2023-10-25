package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.contract.CreateContractCommand;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.ContractProvider;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.rest.model.ContractResponse;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContractResourceTest extends TestSetup {

    private ContractResource subject;

    @Mock
    private AccountProvider accountProvider;
    @Mock
    private ContractProvider contractProvider;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private TransactionScheduleProvider scheduleProvider;

    private FilterFactory filterFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        filterFactory = generateFilterMock();
        subject = new ContractResource(accountProvider, contractProvider, scheduleProvider, filterFactory);

        new EventBus(applicationEventPublisher);
    }

    @Test
    void list() {
        when(contractProvider.lookup()).thenReturn(Collections.List(
                Contract.builder()
                        .id(1L)
                        .name("Contract 1")
                        .startDate(LocalDate.of(2019, 2, 1))
                        .endDate(LocalDate.of(2019, 2, 1))
                        .build(),
                Contract.builder()
                        .id(2L)
                        .name("Contract 2")
                        .terminated(true)
                        .startDate(LocalDate.of(2019, 2, 1))
                        .endDate(LocalDate.of(2019, 2, 1))
                        .build()
        ));

        var response = subject.list();

        assertThat(response.getActive()).hasSize(1);
        assertThat(response.getActive().get(0).getId()).isEqualTo(1L);
        assertThat(response.getTerminated()).hasSize(1);
        assertThat(response.getTerminated().get(0).getId()).isEqualTo(2L);
    }

    @Test
    void autocomplete() {
        when(contractProvider.search("cont")).thenReturn(Collections.List(
                Contract.builder()
                        .id(1L)
                        .name("Contract 1")
                        .startDate(LocalDate.of(2019, 2, 1))
                        .endDate(LocalDate.of(2019, 2, 1))
                        .build(),
                Contract.builder()
                        .id(2L)
                        .name("Contract 2")
                        .terminated(true)
                        .startDate(LocalDate.of(2019, 2, 1))
                        .endDate(LocalDate.of(2019, 2, 1))
                        .build()
        ));

        Assertions.assertThat(subject.autocomplete("cont"))
                .hasSize(2)
                .extracting(ContractResponse::getId)
                .containsExactly(1L, 2L);
    }

    @Test
    void create() {
        var account = Account.builder()
                .id(1L)
                .balance(0D)
                .name("Sample account")
                .currency("EUR")
                .build();

        when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));
        when(contractProvider.lookup("Test Contract"))
                .thenReturn(Control.Option(Contract.builder()
                        .id(1L)
                        .name("Test Contract")
                        .company(account)
                        .startDate(LocalDate.of(2019, 2, 1))
                        .endDate(LocalDate.of(2020, 2, 1))
                        .build()));

        var request = ContractCreateRequest.builder()
                .name("Test Contract")
                .company(ContractCreateRequest.EntityRef.builder().id(1L).build())
                .start(LocalDate.of(2019, 2, 1))
                .end(LocalDate.of(2020, 2, 1))
                .build();

        Assertions.assertThat(subject.create(request))
                        .hasFieldOrPropertyWithValue("name", "Test Contract")
                        .hasFieldOrPropertyWithValue("contractAvailable", false)
                        .hasFieldOrPropertyWithValue("company.id", 1L)
                        .hasFieldOrPropertyWithValue("company.name", "Sample account")
                        .hasFieldOrPropertyWithValue("start", LocalDate.of(2019, 2, 1))
                        .hasFieldOrPropertyWithValue("end", LocalDate.of(2020, 2, 1));

        verify(applicationEventPublisher).publishEvent(Mockito.any(CreateContractCommand.class));
    }

    @Test
    void create_accountNotFound() {
        when(accountProvider.lookup(1L)).thenReturn(Control.Option());

        var request = ContractCreateRequest.builder()
                .name("Test Contract")
                .company(ContractCreateRequest.EntityRef.builder().id(1L).build())
                .start(LocalDate.of(2019, 2, 1))
                .end(LocalDate.of(2020, 2, 1))
                .build();

        Assertions.assertThatThrownBy(() -> subject.create(request))
                .isInstanceOf(StatusException.class)
                .hasMessage("No account can be found for 1");
    }

    @Test
    void update() {
        final Contract contract = Mockito.mock(Contract.class);
        var request = ContractCreateRequest.builder()
                .name("Test Contract")
                .company(ContractCreateRequest.EntityRef.builder().id(1L).build())
                .start(LocalDate.of(2019, 2, 1))
                .end(LocalDate.of(2022, 2, 1))
                .build();

        when(contractProvider.lookup(1L)).thenReturn(Control.Option(contract));
        when(contract.getCompany()).thenReturn(Account.builder().user(ACTIVE_USER).build());
        when(contract.getCompany()).thenReturn(Account.builder()
                .id(1L)
                .balance(0D)
                .name("Sample account")
                .user(ACTIVE_USER)
                .currency("EUR")
                .build());

        subject.update(1L, request);

        verify(contract).change(
                "Test Contract",
                null,
                LocalDate.of(2019, 2, 1),
                LocalDate.of(2022, 2, 1));
    }

    @Test
    void update_notFound() {
        var request = ContractCreateRequest.builder()
                .name("Test Contract")
                .company(ContractCreateRequest.EntityRef.builder().id(1L).build())
                .start(LocalDate.of(2019, 2, 1))
                .end(LocalDate.of(2022, 2, 1))
                .build();

        when(contractProvider.lookup(1L)).thenReturn(Control.Option());

        Assertions.assertThatThrownBy(() -> subject.update(1L, request))
                .isInstanceOf(StatusException.class)
                .hasMessage("No contract can be found for 1");
    }

    @Test
    void get() {
        when(contractProvider.lookup(1L)).thenReturn(Control.Option(
                Contract.builder()
                        .id(1L)
                        .name("Test contract")
                        .company(Account.builder()
                                .id(1L)
                                .balance(0D)
                                .name("Sample account")
                                .user(ACTIVE_USER)
                                .currency("EUR")
                                .build())
                        .description("Sample contract")
                        .startDate(LocalDate.of(2019, 1, 1))
                        .endDate(LocalDate.now().plusMonths(1))
                        .build()));

        Assertions.assertThat(subject.get(1L))
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "Test contract")
                .hasFieldOrPropertyWithValue("description", "Sample contract")
                .hasFieldOrPropertyWithValue("start", LocalDate.of(2019, 1, 1))
                .hasFieldOrPropertyWithValue("end", LocalDate.now().plusMonths(1))
                .hasFieldOrPropertyWithValue("company.id", 1L)
                .hasFieldOrPropertyWithValue("company.name", "Sample account")
                .hasFieldOrPropertyWithValue("company.account.currency", "EUR");
    }

    @Test
    void schedule() {
        var contract = Mockito.spy(Contract.builder()
                .id(1L)
                .startDate(LocalDate.of(2020, 1, 1))
                .endDate(LocalDate.of(2022, 1, 1))
                .build());
        var schedule = Mockito.spy(ScheduledTransaction.builder()
                .id(2L)
                .contract(contract)
                .build());
        final Account account = Account.builder().id(1L).build();
        var filterCommand = filterFactory.schedule();

        when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));

        Mockito.doReturn(Control.Option(contract))
                .when(contractProvider)
                .lookup(1L);

        Mockito.doReturn(ResultPage.of(schedule))
                .when(scheduleProvider)
                .lookup(filterCommand);

        subject.schedule(1L, new CreateScheduleRequest(
                new CreateScheduleRequest.ScheduleValueJson(Periodicity.MONTHS, 3),
                new CreateScheduleRequest.EntityRef(1L, null),
                20.2));

        verify(contract).createSchedule(new ScheduleValue(Periodicity.MONTHS, 3), account, 20.2);
        verify(schedule).limitForContract();
    }

    @Test
    void warnExpiry() {
        final Contract contract = Mockito.mock(Contract.class);

        when(contractProvider.lookup(1L)).thenReturn(Control.Option(contract));
        when(contract.getCompany()).thenReturn(Account.builder().user(ACTIVE_USER).build());

        subject.warnExpiry(1L);

        verify(contract).warnBeforeExpires();
    }

    @Test
    void attachment() {
        final Contract contract = Mockito.mock(Contract.class);

        when(contractProvider.lookup(1L)).thenReturn(Control.Option(contract));
        when(contract.getCompany()).thenReturn(Account.builder().user(ACTIVE_USER).build());

        var request = new ContractAttachmentRequest();
        request.setFileCode("file-code-1");

        subject.attachment(1L, request);

        verify(contract).registerUpload("file-code-1");
    }

    @Test
    void delete() {
        final Contract contract = Mockito.mock(Contract.class);

        when(contractProvider.lookup(1L)).thenReturn(Control.Option(contract));
        when(contract.getCompany()).thenReturn(Account.builder().user(ACTIVE_USER).build());

        subject.delete(1L);

        verify(contract).terminate();
    }

}
