package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.adapter.api.TagProvider;
import com.jongsoft.finance.banking.domain.model.Tag;
import com.jongsoft.finance.rest.TagCommandApi;
import com.jongsoft.finance.rest.model.CreateTagRequest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;

@Controller
class TagCommandController implements TagCommandApi {

    private final Logger logger;
    private final TagProvider tagProvider;

    TagCommandController(TagProvider tagProvider) {
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

        Tag.create(createTagRequest.getName());
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
