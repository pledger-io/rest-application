package com.jongsoft.finance;

import com.jongsoft.finance.core.adapter.api.CurrentUserProvider;
import com.jongsoft.finance.core.adapter.api.Encoder;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.io.IOUtils;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Tag("database")
@MicronautTest(
        environments = {"jpa", "h2", "test", "test-jpa"}, startApplication = false)
public abstract class JpaTestSetup {

    private final Logger log;

    @Inject
    private EntityManager entityManager;

    @Inject
    private AuthenticationFacade authenticationFacade;

    public JpaTestSetup() {
        this.log = LoggerFactory.getLogger(getClass());
    }

    @BeforeEach
    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
    }

    protected void loadDataset(String... files) {
        try {
            for (String file : files) {
                log.info("Loading dataset file " + file);

                var stream = getClass().getClassLoader().getResourceAsStream(file);
                if (stream == null) {
                    Assertions.fail("Could not load dataset " + file);
                }

                var sql = IOUtils.readText(new BufferedReader(new InputStreamReader(stream)));

                entityManager.createNativeQuery(sql).executeUpdate();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @MockBean
    @Replaces(Encoder.class)
    public Encoder encoder() {
        return Mockito.mock(Encoder.class);
    }

    @MockBean
    @Replaces
    public AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
