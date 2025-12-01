package com.jongsoft.finance.rest;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.rest.model.FileResponse;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.multipart.CompletedFileUpload;

import jakarta.validation.Valid;

import org.slf4j.Logger;

import java.io.IOException;

@Controller
public class FilesController implements FilesApi {

    private final Logger logger;
    private final StorageService storageService;

    public FilesController(StorageService storageService) {
        this.storageService = storageService;
        this.logger = org.slf4j.LoggerFactory.getLogger(FilesApi.class);
    }

    @Override
    public HttpResponse<Void> deleteFile(String fileCode) {
        logger.info("Deleting file {}.", fileCode);

        storageService.remove(fileCode);
        return HttpResponse.noContent();
    }

    @Override
    public byte @Nullable(inherited = true) [] downloadFile(String fileCode) {
        logger.info("Downloading file {}.", fileCode);

        return storageService
                .read(fileCode)
                .getOrThrow(() -> StatusException.notFound("No file found with code " + fileCode));
    }

    @Override
    public HttpResponse<@Valid FileResponse> uploadFile(CompletedFileUpload upload) {
        logger.info("Uploading file {}.", upload.getFilename());

        try {
            var token = storageService.store(upload.getBytes());
            return HttpResponse.created(new FileResponse(token));
        } catch (IOException e) {
            logger.error("Failed to store the file.", e);
            throw StatusException.internalError("Failed to store the file.");
        }
    }
}
