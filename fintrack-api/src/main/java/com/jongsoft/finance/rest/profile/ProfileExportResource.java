package com.jongsoft.finance.rest.profile;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.DataProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.serialized.*;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.function.Supplier;

@Controller("/api/profile/export")
@Secured(SecurityRule.IS_AUTHENTICATED)
@io.swagger.v3.oas.annotations.tags.Tag(name = "User profile")
public class ProfileExportResource {

    private final AuthenticationFacade authenticationFacade;
    private final List<Exportable<?>> exportable;
    private final List<DataProvider<?>> dataProviders;
    private final StorageService storageService;
    private final TransactionProvider transactionProvider;
    private final FilterFactory filterFactory;

    public ProfileExportResource(AuthenticationFacade authenticationFacade, List<Exportable<?>> exportable, List<DataProvider<?>> dataProviders, StorageService storageService, TransactionProvider transactionProvider, FilterFactory filterFactory) {
        this.authenticationFacade = authenticationFacade;
        this.exportable = exportable;
        this.dataProviders = dataProviders;
        this.storageService = storageService;
        this.transactionProvider = transactionProvider;
        this.filterFactory = filterFactory;
    }

    @Get
    @Operation(
            summary = "Export to JSON",
            description = "Exports the profile of the authenticated user to JSON",
            operationId = "exportProfile"
    )
    public HttpResponse<ExportJson> export() {
        var exportFileName = authenticationFacade.authenticated() + "-profile.json";
        var exportJson = ExportJson.builder()
                .accounts(lookupAllOf(Account.class)
                        .map(account -> AccountJson.fromDomain(
                                account, loadFromStorage(account.getImageFileToken())))
                        .toJava())
                .budgetPeriods(lookupAllOf(Budget.class).map(BudgetJson::fromDomain).toJava())
                .categories(lookupAllOf(Category.class).map(CategoryJson::fromDomain).toJava())
                .tags(lookupAllOf(Tag.class).map(Tag::name).toJava())
                .contracts(lookupAllOf(Contract.class)
                        .map(c -> ContractJson.fromDomain(c, loadFromStorage(c.getFileToken())))
                        .toJava())
                .rules(lookupAllOf(TransactionRule.class)
                        .map(rule -> RuleConfigJson.RuleJson.fromDomain(rule, this::loadRelation))
                        .toJava())
                .transactions(lookupRelevantTransactions())
                .build();

        return HttpResponse.ok(exportJson)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportFileName + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    private Supplier<byte[]> loadFromStorage(String fileToken) {
        return () -> storageService.read(fileToken).getOrSupply(() -> new byte[0]);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> Sequence<T> lookupAllOf(Class<T> forClass) {
        for (Exportable exporter : exportable) {
            if (exporter.supports(forClass)) {
                return ((Exportable<T>) exporter).lookup();
            }
        }

        return Collections.List();
    }

    private List<TransactionJson> lookupRelevantTransactions() {
        // we also want to export all opening balance transactions for liability accounts
        var filter = filterFactory.transaction()
                .page(0)
                .pageSize(Integer.MAX_VALUE)
                .description("Opening balance", true);

        return transactionProvider.lookup(filter)
                .content()
                .map(TransactionJson::fromDomain)
                .toJava();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String loadRelation(RuleColumn column, String value) {
        if (column == RuleColumn.TAGS) {
            return value;
        }

        Class<?> genericType = switch (column) {
            case TO_ACCOUNT, SOURCE_ACCOUNT, CHANGE_TRANSFER_FROM, CHANGE_TRANSFER_TO -> Account.class;
            case CATEGORY -> Category.class;
            case BUDGET -> Budget.Expense.class;
            case CONTRACT -> Contract.class;
            default -> throw new IllegalArgumentException("Unsupported type");
        };

        for (DataProvider provider : dataProviders) {
            if (provider.supports(genericType)) {
                return provider.lookup(Long.parseLong(value))
                        .get().toString();
            }
        }

        return null;
    }

}
