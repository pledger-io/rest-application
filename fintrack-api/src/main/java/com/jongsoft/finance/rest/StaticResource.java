package com.jongsoft.finance.rest;

import io.micronaut.core.io.ResourceResolver;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Controller
@Secured(SecurityRule.IS_ANONYMOUS)
public class StaticResource {
    private final Logger log = LoggerFactory.getLogger(StaticResource.class);

    @Inject
    ResourceResolver resourceResolver;

    @Get
    @Operation(hidden = true)
    public HttpResponse<?> index() throws URISyntaxException {
        return HttpResponse.redirect(new URI("/ui/dashboard"));
    }

    @Get("/favicon.ico")
    @Operation(hidden = true)
    public HttpResponse<?> favicon() {
        Optional<InputStream> indexHtml = resourceResolver.getResourceAsStream("classpath:public/assets/favicon.ico");
        if (indexHtml.isPresent()) {
            return HttpResponse.ok(new StreamedFile(indexHtml.get(), MediaType.IMAGE_X_ICON_TYPE));
        } else {
            return HttpResponse.notFound("Favicon not found");
        }
    }

}
