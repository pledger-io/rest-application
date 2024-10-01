package com.jongsoft.finance.rest;

import com.jongsoft.finance.core.exception.StatusException;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Controller
@Secured(SecurityRule.IS_ANONYMOUS)
public class StaticResource {
    private final Logger log = LoggerFactory.getLogger(StaticResource.class);

    @Inject
    ResourceResolver res;

    @Get
    @Operation(hidden = true)
    public HttpResponse<?> index() throws URISyntaxException {
        return HttpResponse.redirect(new URI("/ui/dashboard"));
    }

    @Get("/favicon.ico")
    @Operation(hidden = true)
    public HttpResponse<?> favicon() {
        return resource("assets/favicon.ico");
    }

    @Operation(hidden = true)
    @Get("/ui/{path:([^\\.]+)$}")
    @Produces(MediaType.TEXT_HTML)
    public HttpResponse<?> refresh(@PathVariable String path) {
        return resource("index.html");
    }

    @Operation(hidden = true)
    @Get("/ui/{path:(.+)\\.[\\w]+$}")
    public HttpResponse<?> resource(@PathVariable String path) {
        log.info("Loading static resource: {}", path);

        var resource = res.getResource("classpath:public/" + path);
        if (resource.isEmpty()) {
            return HttpResponse.notFound("Resource at path " + path  + " is not found on the server");
        }

        return loadFromUri(resource.get());
    }

    private HttpResponse<?> loadFromUri(URL uri) {
        var streamedFile = new StreamedFile(uri);

        try {
            return HttpResponse.ok(
                    streamedFile.getInputStream().readAllBytes())
                    .contentType(streamedFile.getMediaType())
                    .characterEncoding("utf-8");
        } catch (IOException e) {
            throw StatusException.internalError(e.getMessage());
        }
    }

}
