package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.domain.transaction.TagProvider;
import com.jongsoft.finance.rest.model.TagResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;

import javax.annotation.Nullable;
import javax.validation.Valid;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Transactions")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/transactions/tags")
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
            operationId = "createTag"
    )
    Single<TagResponse> create(@Valid @Body String tag) {
        return Single.just(currentUserProvider.currentUser().createTag(tag))
                .map(TagResponse::new);
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
