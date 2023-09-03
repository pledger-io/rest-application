package com.jongsoft.finance.jpa;

import com.jongsoft.finance.core.Encoder;
import io.micronaut.core.io.IOUtils;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@MicronautTest(
        environments = "application"
)
public abstract class JpaTestSetup {

    private final Logger log;

    @Inject
    private EntityManager entityManager;

    public JpaTestSetup() {
        this.log = LoggerFactory.getLogger(getClass());
    }

    protected void loadDataset(String... files) {
        try {
            for (String file : files) {
                log.info("Loading dataset file " + file);

                var stream = getClass().getClassLoader().getResourceAsStream(file);
                if (stream == null) {
                    Assertions.assertFalse(true, "Could not load dataset " + file);
                }

                var sql = IOUtils.readText(new BufferedReader(new InputStreamReader(stream)));

                entityManager.createNativeQuery(sql).executeUpdate();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @MockBean
    public Encoder encoder() {
        return Mockito.mock(Encoder.class);
    }

}
