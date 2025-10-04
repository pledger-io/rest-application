package com.jongsoft.finance.rest.transaction;

import static com.jongsoft.finance.rest.ApiConstants.TAG_TRANSACTION_TAGGING;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.finance.rest.model.TagResponse;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.finance.security.CurrentUserProvider;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;

import java.util.List;

@Secured(AuthenticationRoles.IS_AUTHENTICATED)
@Controller("/api/transactions/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = TAG_TRANSACTION_TAGGING)
public class TransactionTagResource {

    private final SettingProvider settingProvider;
    private final TagProvider tagProvider;
    private final FilterFactory filterFactory;

    private final CurrentUserProvider currentUserProvider;

    public TransactionTagResource(
            SettingProvider settingProvider,
            TagProvider tagProvider,
            FilterFactory filterFactory,
            CurrentUserProvider currentUserProvider) {
        this.settingProvider = settingProvider;
        this.tagProvider = tagProvider;
        this.filterFactory = filterFactory;
        this.currentUserProvider = currentUserProvider;
    }

    @Post
    @Operation(
            summary = "Create tag",
            description = "Creates a new tag into the system",
            operationId = "createTag")
    TagResponse create(@Valid @Body TagCreateRequest tag) {
        return new TagResponse(currentUserProvider.currentUser().createTag(tag.tag()));
    }

    @Get
    @Operation(
            operationId = "getTags",
            summary = "List tags",
            description = "Get all tags available in the system.")
    List<TagResponse> list() {
        return tagProvider.lookup().map(TagResponse::new).toJava();
    }

    @Delete("/{tag}")
    @Operation(
            operationId = "deleteTag",
            summary = "Delete tag",
            description =
                    "Removes a tag from the system, this prevents it being used in updates. But"
                            + " will not remove old relations between tags and transactions.")
    void delete(@PathVariable String tag) {
        tagProvider
                .lookup(tag)
                .ifPresent(Tag::archive)
                .elseThrow(
                        () -> StatusException.notFound("No active tag found with contents " + tag));
    }

    @Get("/auto-complete{?token}")
    @Operation(
            summary = "Search tag",
            description = "Look for tags with the partial token in the name",
            operationId = "lookupTags")
    List<TagResponse> autoCompleteTag(@Nullable String token) {
        var filter = filterFactory
                .tag()
                .name(token, false)
                .page(0, settingProvider.getAutocompleteLimit());

        return tagProvider.lookup(filter).content().map(TagResponse::new).toJava();
    }
}
