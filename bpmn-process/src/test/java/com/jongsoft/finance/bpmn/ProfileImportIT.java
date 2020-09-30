package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.user.*;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.API;
import io.reactivex.Maybe;
import org.camunda.bpm.engine.ProcessEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileImportIT extends ProcessTestSetup {

    @Inject
    private ProcessEngine processEngine;
    @Inject
    private CurrentUserProvider authenticationFacade;

    @Inject
    private UserProvider userProvider;

    @Inject
    private AccountProvider accountProvider;
    @Inject
    private CategoryProvider categoryProvider;
    @Inject
    private StorageService storageService;

    @BeforeEach
    void setup() {
        Mockito.reset(accountProvider, authenticationFacade, categoryProvider, storageService, userProvider);

        UserAccount userAccount = UserAccount.builder()
                .id(1L)
                .username("test-user")
                .password("12345")
                .roles(API.List(new Role("admin")))
                .build();
        Mockito.when(authenticationFacade.currentUser()).thenReturn(userAccount);
        Mockito.when(userProvider.lookup("test-user")).thenReturn(API.Option(userAccount));
    }

    @Test
    void run() {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("profile-test/profile-export.json");
        String configContent = new BufferedReader(new InputStreamReader(resourceAsStream)).lines().collect(Collectors.joining("\n"));

        final Account accountDemo = Account.builder()
                .id(1L)
                .build();
        final Account accountShop = Account.builder()
                .id(2L)
                .build();
        final Account accountBoss = Account.builder()
                .id(3L)
                .build();

        StringBuilder ruleJsonString = new StringBuilder();

        Mockito.when(storageService.read("my-sample-token")).thenReturn(configContent.getBytes());
        Mockito.when(storageService.store(Mockito.any())).then((Answer<String>) invocation -> {
            byte[] param = invocation.getArgument(0);
            ruleJsonString.append(new String(param));
            return "my-json-token";
        });
        Mockito.when(storageService.read("my-json-token")).then((Answer<byte[]>) inv -> ruleJsonString.toString().getBytes());

        Mockito.when(categoryProvider.lookup("Salary")).thenReturn(
                Maybe.empty(),
                Maybe.just(Category.builder()
                        .id(1L)
                        .build()));
        Mockito.when(categoryProvider.lookup("Groceries")).thenReturn(
                Maybe.empty(),
                Maybe.just(Category.builder()
                        .id(2L)
                        .build()));
        Mockito.when(categoryProvider.lookup("Car")).thenReturn(
                Maybe.empty(),
                Maybe.just(Category.builder()
                        .id(3L)
                        .build()));
        Mockito.when(accountProvider.lookup("Boss & Co."))
                .thenReturn(Maybe.empty())
                .thenReturn(Maybe.just(accountBoss));
        Mockito.when(accountProvider.lookup("Demo checking account"))
                .thenReturn(Maybe.empty())
                .thenReturn(Maybe.just(accountDemo));
        Mockito.when(accountProvider.lookup("Groceries are us"))
                .thenReturn(Maybe.empty())
                .thenReturn(Maybe.just(accountShop));

        processEngine.getRuntimeService().createProcessInstanceByKey("ImportUserProfile")
                .setVariable("storageToken", "my-sample-token")
                .setVariable("username", authenticationFacade.currentUser().getUsername())
                .execute();

        waitUntilNoActiveJobs(processEngine, 1000);

        assertThat(accountDemo.getName()).isEqualTo("Demo checking account");
        assertThat(accountDemo.getCurrency()).isEqualTo("EUR");
        assertThat(accountDemo.getType()).isEqualTo("default");

        assertThat(accountShop.getName()).isEqualTo("Groceries are us");
        assertThat(accountShop.getCurrency()).isEqualTo("EUR");
        assertThat(accountShop.getType()).isEqualTo("creditor");
    }

}
