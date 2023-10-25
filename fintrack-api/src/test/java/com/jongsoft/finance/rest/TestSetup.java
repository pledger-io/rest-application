package com.jongsoft.finance.rest;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.*;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import jakarta.inject.Inject;
import org.jboss.aerogear.security.otp.api.Base32;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Currency;

public class TestSetup {

    protected final UserAccount ACTIVE_USER = UserAccount.builder()
            .id(1L)
            .username("test-user")
            .password("1234")
            .theme("dark")
            .primaryCurrency(Currency.getInstance("EUR"))
            .secret(Base32.random())
            .roles(Collections.List(new Role("admin")))
            .build();

    protected String ACTIVE_USER_TOKEN = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJmaW4tdHJhY2siLCJzdWIiOiJ0ZXN0LXVzZXIiLCJuYmYiOjE2OTgyNDMzMTYsImlhdCI6MTY5ODI0MzMxNiwiZXhwIjozNjk4MjQzMzE2LCJyb2xlcyI6WyJhZG1pbiIsImFjY291bnRhbnQiXX0.xzEVTquxmHM9WTyaCTfurjnRzAgmClbBdtQhcQ6ESOnPRj0mF3Xz6j1lPEC_DwntVa29RxXRWKSzP_sRfjSg3i2xT9Xmb0-E3OaILWUScoGk9vy6cZSJt9t7v82qE3I_jd6IFk9WJu_NcRefS-O_f1ilmU9jRABom6I0DZYfVCGixQUdXY3WiQ46X9aCIlzb7TboMs5ArSehrQY7tZgdP-uSS8QkcFVT__lm6CgKApsUAs8u0QBVUgfRBYSuWnGRern49xOq74F-WBCn_xY9jfKFkstQyGMYYpVWziDfM02eLyXcczClk86jyIu3Csan2v5rBH55erLH2_tJNLCsDw";

    protected FilterFactory generateFilterMock() {
        final FilterFactory filterFactory = Mockito.mock(FilterFactory.class);
        Mockito.when(filterFactory.transaction())
                .thenReturn(Mockito.mock(TransactionProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.account())
                .thenReturn(Mockito.mock(AccountProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.expense())
                .thenReturn(Mockito.mock(ExpenseProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.category())
                .thenReturn(Mockito.mock(CategoryProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.tag())
                .thenReturn(Mockito.mock(TagProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.schedule())
                .thenReturn(Mockito.mock(TransactionScheduleProvider.FilterCommand.class, InvocationOnMock::getMock));
        return filterFactory;
    }

    @Inject
    protected CurrentUserProvider currentUserProvider;
    @Inject
    protected UserProvider userProvider;


    @BeforeEach
    void initialize() {
        System.out.println("Initializing test setup");

        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType("application/json")
                // override the default object mapper to use the JavaTimeModule
                .setConfig(RestAssured.config().objectMapperConfig(
                        ObjectMapperConfig.objectMapperConfig()
                                .jackson2ObjectMapperFactory(
                                        (type, s) -> {
                                            var mapper = new ObjectMapper();
                                            var module = new SimpleModule();
                                            module.addSerializer(LocalDate.class, new JsonSerializer<>() {
                                                @Override
                                                public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                                                    gen.writeString(value.toString());
                                                }
                                            });
                                            mapper.registerModule(module);
                                            return mapper;
                                        }
                                )
                ))
                .build();

        Mockito.when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);
        Mockito.when(userProvider.lookup(ACTIVE_USER.getUsername())).thenReturn(Control.Option(ACTIVE_USER));
    }

    @AfterEach
    void after() {
        System.out.println("Resetting test setup");

        Mockito.reset(currentUserProvider, userProvider);
    }

    @Replaces
    @MockBean
    protected AccountProvider accountProvider() {
        return Mockito.mock(AccountProvider.class);
    }

    @Replaces
    @MockBean
    protected CurrentUserProvider currentUserProvider() {
        return Mockito.mock(CurrentUserProvider.class);
    }

    @Replaces
    @MockBean
    protected UserProvider userProvider() {
        return Mockito.mock(UserProvider.class);
    }
}
