package com.jongsoft.finance.rest.file;

import com.jongsoft.finance.StorageService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

@Tag(name = "Attachments")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/attachment")
public class FileResource {

    private final StorageService storageService;

    public FileResource(StorageService storageService) {
        this.storageService = storageService;
    }

    @Post
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    UploadResponse upload(@Body CompletedFileUpload upload) throws IOException {
        var token = storageService.store(IOUtils.toByteArray(upload.getInputStream()));
        return new UploadResponse(token);
    }

    @Get("/{fileCode}")
    byte[] download(@PathVariable String fileCode) {
        return storageService.read(fileCode);
    }

}
