package com.jongsoft.finance.rest;

import io.micronaut.core.io.ResourceResolver;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.server.types.files.StreamedFile;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReactControllerTest {

    @Test
    void testIndexReturnsStreamedFileWhenResourceExists() {
        // Arrange
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        ReactController reactController = new ReactController(resourceResolver);
        InputStream mockStream = new ByteArrayInputStream("<html>Mock index.html</html>".getBytes());
        when(resourceResolver.getResourceAsStream("classpath:public/index.html")).thenReturn(Optional.of(mockStream));

        // Act
        HttpResponse<?> response = reactController.index();

        // Assert
        assertEquals(HttpResponse.ok().body(new StreamedFile(mockStream, MediaType.TEXT_HTML_TYPE)).getStatus(), response.getStatus());
        verify(resourceResolver, times(1)).getResourceAsStream("classpath:public/index.html");
    }

    @Test
    void testIndexReturnsNotFoundWhenResourceDoesNotExist() {
        // Arrange
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        ReactController reactController = new ReactController(resourceResolver);
        when(resourceResolver.getResourceAsStream("classpath:public/index.html")).thenReturn(Optional.empty());

        // Act
        HttpResponse<?> response = reactController.index();

        // Assert
        assertEquals(HttpResponse.notFound("React app not found").getStatus(), response.getStatus());
        verify(resourceResolver, times(1)).getResourceAsStream("classpath:public/index.html");
    }
}
