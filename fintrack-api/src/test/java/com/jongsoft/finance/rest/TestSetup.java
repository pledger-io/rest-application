package com.jongsoft.finance.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.providers.*;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.event.ApplicationEventPublisher;
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

    @Inject
    protected CurrentUserProvider currentUserProvider;
    @Inject
    protected AuthenticationFacade authenticationFacade;
    @Inject
    protected UserProvider userProvider;
    @Inject
    protected FilterFactory filterFactory;

    @BeforeEach
    void initialize() {
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType("application/json")
                // override the default object mapper to use the JavaTimeModule
                .setConfig(RestAssured.config()
                        .objectMapperConfig(
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
                                                    mapper.setVisibility(mapper.getVisibilityChecker()
                                                            .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
                                                    return mapper;
                                                }
                                        ))
                        .logConfig(RestAssured.config().getLogConfig()
                                .enableLoggingOfRequestAndResponseIfValidationFails()))
                .build();

        Mockito.when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);
        Mockito.when(authenticationFacade.authenticated()).thenReturn(ACTIVE_USER.getUsername());
        Mockito.when(userProvider.lookup(ACTIVE_USER.getUsername())).thenReturn(Control.Option(ACTIVE_USER));

        // initialize the event bus
        new EventBus(Mockito.mock(ApplicationEventPublisher.class));
    }

    @AfterEach
    void after() {
        Mockito.reset(currentUserProvider, userProvider, filterFactory);
    }

    @Replaces
    @MockBean
    CurrentUserProvider currentUserProvider() {
        return Mockito.mock(CurrentUserProvider.class);
    }

    @Replaces
    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }

    @Replaces
    @MockBean
    UserProvider userProvider() {
        return Mockito.mock(UserProvider.class);
    }

    @MockBean
    @Replaces
    FilterFactory generateFilterMock() {
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
}
