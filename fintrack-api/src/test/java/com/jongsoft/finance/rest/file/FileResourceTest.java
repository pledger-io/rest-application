package com.jongsoft.finance.rest.file;

import com.jongsoft.finance.StorageService;
import io.micronaut.http.multipart.CompletedFileUpload;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

class FileResourceTest {

    private FileResource subject;

    @Mock
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        subject = new FileResource(storageService);
    }

    @Test
    void upload() throws IOException {
        Mockito.when(storageService.store("sample-data".getBytes())).thenReturn("sample-token");

        var upload = Mockito.mock(CompletedFileUpload.class);
        Mockito.when(upload.getInputStream()).thenReturn(getClass().getResourceAsStream("application.yml"));

        subject.upload(upload);

        Mockito.verify(storageService).store(Mockito.any());
    }

    @Test
    void download() {
        Mockito.when(storageService.read("fasjkdh8nfasd8")).thenReturn("sample-token".getBytes());

        var response = subject.download("fasjkdh8nfasd8");

        Assertions.assertThat(response).isEqualTo("sample-token".getBytes());
        Mockito.verify(storageService).read("fasjkdh8nfasd8");
    }
}