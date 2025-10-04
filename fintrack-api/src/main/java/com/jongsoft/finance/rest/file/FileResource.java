package com.jongsoft.finance.rest.file;

import static com.jongsoft.finance.rest.ApiConstants.TAG_ATTACHMENTS;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.security.AuthenticationRoles;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;

@Tag(name = TAG_ATTACHMENTS)
@Controller("/api/attachment")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
public class FileResource {

    private final StorageService storageService;

    public FileResource(StorageService storageService) {
        this.storageService = storageService;
    }

    @Post
    @Status(HttpStatus.CREATED)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Upload attachment",
            description =
                    "Upload a file so that it can be attached to one of the entities in FinTrack")
    UploadResponse upload(@Body CompletedFileUpload upload) throws IOException {
        var token = storageService.store(upload.getBytes());
        return new UploadResponse(token);
    }

    @Get(value = "/{fileCode}", consumes = MediaType.ALL, produces = MediaType.ALL)
    @Operation(
            summary = "Download attachment",
            description = "Download an existing attachment, if file encryption is enabled this will"
                    + " throw an exception if the current user did not upload the file.")
    byte[] download(@PathVariable String fileCode) {
        return storageService.read(fileCode).get();
    }

    @Delete("/{fileCode}")
    @Status(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete attachment",
            description = "Delete an existing attachment, if file encryption is enabled this will"
                    + " throw an exception if the current user did not upload the file.")
    void delete(@PathVariable String fileCode) {
        storageService.remove(fileCode);
    }
}
