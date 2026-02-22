package com.jongsoft.finance.core.adapter.ui;

import io.micronaut.core.io.ResourceResolver;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import jakarta.inject.Inject;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Controller
@Secured(SecurityRule.IS_ANONYMOUS)
public class StaticController {

    @Inject
    ResourceResolver resourceResolver;

    @Get
    public HttpResponse<?> index() throws URISyntaxException {
        return HttpResponse.redirect(new URI("/ui/dashboard"));
    }

    @Get("/favicon.ico")
    public HttpResponse<?> favicon() {
        Optional<InputStream> indexHtml =
                resourceResolver.getResourceAsStream("classpath:public/assets/favicon.ico");
        if (indexHtml.isPresent()) {
            return HttpResponse.ok(new StreamedFile(indexHtml.get(), MediaType.IMAGE_X_ICON_TYPE));
        } else {
            return HttpResponse.notFound("Favicon not found");
        }
    }
}
