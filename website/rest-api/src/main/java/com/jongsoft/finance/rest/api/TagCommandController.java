package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.finance.rest.model.CreateTagRequest;
import com.jongsoft.finance.security.CurrentUserProvider;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;

@Controller
class TagCommandController implements TagCommandApi {

    private final Logger logger;
    private final CurrentUserProvider currentUserProvider;
    private final TagProvider tagProvider;

    TagCommandController(CurrentUserProvider currentUserProvider, TagProvider tagProvider) {
        this.currentUserProvider = currentUserProvider;
        this.tagProvider = tagProvider;
        this.logger = org.slf4j.LoggerFactory.getLogger(TagCommandController.class);
    }

    @Override
    public HttpResponse<Void> createTag(CreateTagRequest createTagRequest) {
        logger.info("Creating tag {}.", createTagRequest.getName());

        if (tagProvider.lookup(createTagRequest.getName()).isPresent()) {
            throw StatusException.badRequest(
                    "Tag with name " + createTagRequest.getName() + " already exists");
        }

        currentUserProvider.currentUser().createTag(createTagRequest.getName());
        return HttpResponse.noContent();
    }

    @Override
    public HttpResponse<Void> deleteTag(String name) {
        logger.info("Deleting tag {}.", name);

        var tag = tagProvider
                .lookup(name)
                .getOrThrow(() -> StatusException.notFound("Tag is not found"));
        tag.archive();
        return HttpResponse.noContent();
    }
}
