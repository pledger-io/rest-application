package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.finance.rest.model.TagResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.validation.Valid;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/transactions/tags")
@RequiredArgsConstructor(onConstructor_ = @Inject)
@io.swagger.v3.oas.annotations.tags.Tag(name = "Transactions")
public class TransactionTagResource {

    private final SettingProvider settingProvider;
    private final TagProvider tagProvider;
    private final FilterFactory filterFactory;

    private final CurrentUserProvider currentUserProvider;

    @Post
    @Operation(
            summary = "Create tag",
            description = "Creates a new tag into the system",
            operationId = "createTag"
    )
    Single<TagResponse> create(@Valid @Body TagCreateRequest tag) {
        return Single.just(currentUserProvider.currentUser().createTag(tag.getTag()))
                .map(TagResponse::new);
    }

    @Get
    @Operation(
            operationId = "getTags",
            summary = "List tags",
            description = "Get all tags available in the system."
    )
    Flowable<TagResponse> list() {
        return Flowable.create(emitter -> {
            tagProvider.lookup()
                    .map(TagResponse::new)
                    .forEach(emitter::onNext);

            emitter.onComplete();
        }, BackpressureStrategy.DROP);
    }

    @Get("/auto-complete{?token}")
    @Operation(
            summary = "Search tag",
            description = "Look for tags with the partial token in the name",
            operationId = "lookupTags"
    )
    Flowable<TagResponse> autoCompleteTag(@Nullable String token) {
        var filter = filterFactory.tag()
                .name(token, false)
                .pageSize(settingProvider.getAutocompleteLimit());

        var response = tagProvider.lookup(filter).content();

        return Flowable.fromIterable(response.map(TagResponse::new));
    }

}
