package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.ContractProvider;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Contract Resource")
class ContractResourceTest extends TestSetup {

    @Inject
    private AccountProvider accountProvider;
    @Inject
    private ContractProvider contractProvider;
    @Inject
    private TransactionScheduleProvider scheduleProvider;

    @Replaces
    @MockBean
    AccountProvider accountProvider() {
        return Mockito.mock(AccountProvider.class);
    }

    @Replaces
    @MockBean
    ContractProvider contractProvider() {
        return Mockito.mock(ContractProvider.class);
    }

    @Replaces
    @MockBean
    TransactionScheduleProvider scheduleProvider() {
        return Mockito.mock(TransactionScheduleProvider.class);
    }

    @Test
    @DisplayName("List all contracts")
    void list(RequestSpecification spec) {
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

        // @formatter:off
        spec
            .when()
                .get("/api/contracts")
            .then()
                .statusCode(200)
                .body("active", org.hamcrest.Matchers.hasSize(1))
                .body("active[0].id", org.hamcrest.Matchers.equalTo(1))
                .body("terminated", org.hamcrest.Matchers.hasSize(1))
                .body("terminated[0].id", org.hamcrest.Matchers.equalTo(2));
        // @formatter:on
    }

    @Test
    @DisplayName("Autocomplete contracts")
    void autocomplete(RequestSpecification spec) {
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

        // @formatter:off
        spec
            .when()
                .get("/api/contracts/auto-complete?token=cont")
            .then()
                .statusCode(200)
                .body("$", org.hamcrest.Matchers.hasSize(2))
                .body("[0].id", org.hamcrest.Matchers.equalTo(1))
                .body("[1].id", org.hamcrest.Matchers.equalTo(2));
        // @formatter:on
    }

    @Test
    @DisplayName("Create contract")
    void create(RequestSpecification spec) {
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

        // @formatter:off
        spec
            .given()
                .body("""
                        {
                            "name": "Test Contract",
                            "company": {
                                "id": 1
                            },
                            "start": "2019-02-01",
                            "end": "2020-02-01"
                        }""")
            .when()
                .put("/api/contracts")
            .then()
                .statusCode(201)
                .body("id", org.hamcrest.Matchers.equalTo(1))
                .body("name", org.hamcrest.Matchers.equalTo("Test Contract"))
                .body("contractAvailable", org.hamcrest.Matchers.equalTo(false))
                .body("company.id", org.hamcrest.Matchers.equalTo(1))
                .body("company.name", org.hamcrest.Matchers.equalTo("Sample account"))
                .body("start", org.hamcrest.Matchers.equalTo("2019-02-01"))
                .body("end", org.hamcrest.Matchers.equalTo("2020-02-01"));
        // @formatter:on
    }

    @Test
    @DisplayName("Create contract with no account")
    void create_accountNotFound(RequestSpecification spec) {
        when(accountProvider.lookup(1L)).thenReturn(Control.Option());

        // @formatter:off
        spec
            .given()
                .body("""
                        {
                            "name": "Test Contract",
                            "company": {
                                "id": 1
                            },
                            "start": "2019-02-01",
                            "end": "2020-02-01"
                        }""")
            .when()
                .put("/api/contracts")
            .then()
                .statusCode(404)
                .body("message", org.hamcrest.Matchers.equalTo("No account can be found for 1"));
        // @formatter:on
    }

    @Test
    @DisplayName("Update existing contract")
    void update(RequestSpecification spec) {
        final Contract contract = Mockito.mock(Contract.class);

        when(contractProvider.lookup(1L)).thenReturn(Control.Option(contract));
        when(contract.getCompany()).thenReturn(Account.builder().user(ACTIVE_USER).build());
        when(contract.getCompany()).thenReturn(Account.builder()
                .id(1L)
                .balance(0D)
                .name("Sample account")
                .user(ACTIVE_USER)
                .currency("EUR")
                .build());

        // @formatter:off
        spec
            .given()
                .body("""
                        {
                            "name": "Test Contract",
                            "company": {
                                "id": 1
                            },
                            "start": "2019-02-01",
                            "end": "2022-02-01"
                        }""")
            .when()
                .post("/api/contracts/1")
            .then()
                .statusCode(200)
                .body("contractAvailable", org.hamcrest.Matchers.equalTo(false))
                .body("company.id", org.hamcrest.Matchers.equalTo(1))
                .body("company.name", org.hamcrest.Matchers.equalTo("Sample account"));
        // @formatter:on

        verify(contract).change(
                "Test Contract",
                null,
                LocalDate.of(2019, 2, 1),
                LocalDate.of(2022, 2, 1));
    }

    @Test
    @DisplayName("Update contract not found")
    void update_notFound(RequestSpecification spec) {
        when(contractProvider.lookup(1L)).thenReturn(Control.Option());

        // @formatter:off
        spec
            .given()
                .body("""
                        {
                            "name": "Test Contract",
                            "company": {
                                "id": 1
                            },
                            "start": "2019-02-01",
                            "end": "2022-02-01"
                        }""")
            .when()
                .post("/api/contracts/{id}", 1)
            .then()
                .statusCode(404)
                .body("message", org.hamcrest.Matchers.equalTo("No contract can be found for 1"));
        // @formatter:on
    }

    @Test
    @DisplayName("Get contract")
    void get(RequestSpecification spec) {
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

        // @formatter:off
        spec
            .when()
                .get("/api/contracts/{id}", 1)
            .then()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.equalTo(1))
                .body("name", org.hamcrest.Matchers.equalTo("Test contract"))
                .body("description", org.hamcrest.Matchers.equalTo("Sample contract"))
                .body("start", org.hamcrest.Matchers.equalTo("2019-01-01"))
                .body("end", org.hamcrest.Matchers.equalTo(LocalDate.now().plusMonths(1).toString()))
                .body("company.id", org.hamcrest.Matchers.equalTo(1))
                .body("company.name", org.hamcrest.Matchers.equalTo("Sample account"))
                .body("company.account.currency", org.hamcrest.Matchers.equalTo("EUR"));
        // @formatter:on
    }

    @Test
    @DisplayName("Schedule transaction for contract")
    void schedule(RequestSpecification spec) {
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

        // @formatter:off
        spec
            .when()
                .body("""
                        {
                            "amount": 20.2,
                            "source": {
                                "id": 1
                            },
                            "schedule": {
                                "periodicity": "MONTHS",
                                "value": 3
                            }
                        }""")
                .put("/api/contracts/1/schedule")
            .then()
                .statusCode(200);
        // @formatter:on

        verify(contract).createSchedule(new ScheduleValue(Periodicity.MONTHS, 3), account, 20.2);
        verify(schedule).limitForContract();
    }

    @Test
    @DisplayName("Warn before expiry date")
    void warnExpiry(RequestSpecification spec) {
        final Contract contract = Mockito.mock(Contract.class);

        when(contractProvider.lookup(1L)).thenReturn(Control.Option(contract));
        when(contract.getCompany()).thenReturn(Account.builder().user(ACTIVE_USER).build());

        // @formatter:off
        spec
            .when()
                .get("/api/contracts/{id}/expire-warning", 1)
            .then()
                .statusCode(200);
        // @formatter:on

        verify(contract).warnBeforeExpires();
    }

    @Test
    @DisplayName("Attach PDF file for contract")
    void attachment(RequestSpecification spec) {
        final Contract contract = Mockito.mock(Contract.class);

        when(contractProvider.lookup(1L)).thenReturn(Control.Option(contract));
        when(contract.getCompany()).thenReturn(Account.builder().user(ACTIVE_USER).build());

        // @formatter:off
        spec
            .when()
                .body("""
                        {
                            "fileCode": "file-code-1"
                        }""")
                .post("/api/contracts/{id}/attachment", 1)
            .then()
                .statusCode(200);
        // @formatter:on

        verify(contract).registerUpload("file-code-1");
    }

    @Test
    @DisplayName("Delete contract")
    void delete(RequestSpecification spec) {
        final Contract contract = Mockito.mock(Contract.class);

        when(contractProvider.lookup(1L)).thenReturn(Control.Option(contract));
        when(contract.getCompany()).thenReturn(Account.builder().user(ACTIVE_USER).build());

        // @formatter:off
        spec
            .when()
                .delete("/api/contracts/{id}", 1)
            .then()
                .statusCode(200);
        // @formatter:on

        verify(contract).terminate();
    }

}
