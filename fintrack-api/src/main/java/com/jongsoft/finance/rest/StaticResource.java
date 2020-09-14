package com.jongsoft.finance.rest;

import io.micronaut.core.io.ResourceResolver;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Slf4j
@Controller
@Secured(SecurityRule.IS_ANONYMOUS)
public class StaticResource {

    @Inject
    ResourceResolver res;

    @Get
    public HttpResponse<?> index() throws URISyntaxException {
        return HttpResponse.redirect(new URI("/ui/dashboard"));
    }

    @Get("/favicon.ico")
    @Operation(hidden = true)
    public Single<HttpResponse<?>> favicon() {
        return resource("assets/favicon.ico");
    }

    @Operation(hidden = true)
    @Get("/ui/{path:([^\\.]+)$}")
    @Produces(MediaType.TEXT_HTML)
    public HttpResponse<?> refresh(HttpRequest<?> request, @PathVariable String path) throws IOException {
        log.info("Loading web app page: {}", path);

        var indexUri = res.getResource("classpath:public/index.html");
        if (indexUri.isEmpty()) {
            return HttpResponse.notFound("Angular application not correctly bundled");
        } else {
            return loadFromUri(indexUri.get());
        }
    }

    @Operation(hidden = true)
    @Get("/ui/{path:(.+)\\.[\\w]+$}")
    public Single<HttpResponse<?>> resource(@PathVariable String path) {
        log.info("Loading static resource: {}", path);

        return Single.create(emitter -> {
            var actualResource = res.getResource("classpath:public/" + path);
            if (actualResource.isEmpty()) {
                emitter.onSuccess(HttpResponse.notFound(
                        "Resource at path " + path  + " is not found on the server"));
            } else {
                emitter.onSuccess(loadFromUri(actualResource.get()));
            }
        });
    }

    private HttpResponse<?> loadFromUri(URL uri) throws IOException {
        var streamedFile = new StreamedFile(uri);

        return HttpResponse.ok(
                IOUtils.readFully(
                        streamedFile.getInputStream(),
                        (int) streamedFile.getLength()))
                .contentType(streamedFile.getMediaType())
                .characterEncoding("utf-8");
    }

}
