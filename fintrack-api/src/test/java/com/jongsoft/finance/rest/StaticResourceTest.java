package com.jongsoft.finance.rest;

import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.DefaultClassPathResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void refresh() {
        var response = subject.refresh();

        assertThat(new String((byte[]) response.body())).isEqualTo("It works!!!!");
    }

    @Test
    void resource() {
        var response = subject.resource("css/style.css");

        assertThat(new String((byte[]) response.body())).isEqualTo("Style works");
    }

    @Test
    void resource_notFound() {
        var response = subject.resource("css/style-2.css");

        assertThat(response.getStatus().getCode()).isEqualTo(404);
    }
}