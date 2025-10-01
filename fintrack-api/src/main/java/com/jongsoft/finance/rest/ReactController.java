package com.jongsoft.finance.rest;

import static com.jongsoft.finance.rest.ApiConstants.TAG_REACT_APP;

import io.micronaut.core.io.ResourceResolver;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller("/ui")
@Tag(name = TAG_REACT_APP)
@Secured(SecurityRule.IS_ANONYMOUS)
public class ReactController {

    private final ResourceResolver resourceResolver;

    public ReactController(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    /**
     * Serves the React app's index.html file.
     *
     * @return The index.html file
     */
    @Get(produces = MediaType.TEXT_HTML)
    public HttpResponse<?> index() {
        Optional<InputStream> indexHtml =
                resourceResolver.getResourceAsStream("classpath:public/index.html");
        if (indexHtml.isPresent()) {
            return HttpResponse.ok(new StreamedFile(indexHtml.get(), MediaType.TEXT_HTML_TYPE));
        } else {
            return HttpResponse.notFound("React app not found");
        }
    }

    /**
     * Catch-all route to serve the React app for any path under /react/. This allows the React
     * Router to handle client-side routing properly.
     *
     * @return The index.html file
     */
    @Get(uri = "/{path:.*}", produces = MediaType.TEXT_HTML)
    public HttpResponse<?> catchAll(String path) {
        return index();
    }

    @Get(uri = "/favicon.ico")
    public HttpResponse<?> favicon() {
        return loadResource("favicon.ico");
    }

    @Get(uri = "/manifest.json")
    public HttpResponse<?> manifest() {
        return loadResource("manifest.json");
    }

    @Get(uri = "/logo192.png")
    public HttpResponse<?> logo() {
        return loadResource("logo192.png");
    }

    @Get(uri = "/logo512.png")
    public HttpResponse<?> logo_512() {
        return loadResource("logo512.png");
    }

    @Get(uri = "/assets/{path:.*}")
    public HttpResponse<?> getAsset(String path) {
        return loadResource("assets/" + path);
    }

    @Get(uri = "/images/{path:.*}")
    public HttpResponse<?> getImage(String path) {
        return loadResource("images/" + path);
    }

    private HttpResponse<?> loadResource(String path) {
        var assetFile = resourceResolver.getResource("classpath:public/" + path);
        if (assetFile.isPresent()) {
            var streamedFile = new StreamedFile(assetFile.get());
            return HttpResponse.ok(streamedFile.getInputStream())
                    .contentType(streamedFile.getMediaType())
                    .characterEncoding(StandardCharsets.UTF_8);
        } else {
            return HttpResponse.notFound("React app not found");
        }
    }
}
