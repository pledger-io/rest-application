package com.jongsoft.finance.rest;

import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.DefaultClassPathResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StaticResourceTest {

    @Spy
    ResourceResolver resourceResolver = new ResourceResolver(List.of(
            new DefaultClassPathResourceLoader(ClassLoader.getSystemClassLoader())));

    @InjectMocks
    private StaticResource subject = new StaticResource();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void index() throws URISyntaxException {
        var response = subject.index();

        assertThat(response.getHeaders().get("Location")).isEqualTo("/ui/dashboard");
    }

}
