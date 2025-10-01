package com.jongsoft.finance;

import static com.jongsoft.finance.rest.ApiConstants.*;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(
        info =
                @Info(
                        title = "Pledger",
                        version = "2.0.0",
                        description =
                                "Pledger.io is a self-hosted personal finance application that"
                                        + " helps you track your income and expenses.",
                        license =
                                @License(name = "MIT", url = "https://opensource.org/licenses/MIT"),
                        contact =
                                @Contact(
                                        name = "Jong Soft Development",
                                        url = "https://github.com/pledger-io/rest-application")),
        security = @SecurityRequirement(name = "bearer"),
        tags = {
            @Tag(
                    name = TAG_REACT_APP,
                    description =
                            "All methods reserved to fetch the React App embedded in the API."),
            @Tag(
                    name = TAG_ACCOUNTS,
                    description = "API access to fetch accounts of a user in Pledger.io"),
            @Tag(
                    name = TAG_ACCOUNTS_TRANSACTIONS,
                    description =
                            "API access to fetch transactions for any accounts of a user in"
                                    + " Pledger.io"),
            @Tag(
                    name = TAG_BUDGETS,
                    description = "API to get access to budget information for the user."),
            @Tag(
                    name = TAG_CATEGORIES,
                    description = "API to get access to category information for the user."),
            @Tag(
                    name = TAG_CONTRACTS,
                    description = "API to get access to contract information for the user."),
            @Tag(
                    name = TAG_ATTACHMENTS,
                    description =
                            "API for managing file attachments and documents related to"
                                    + " transactions and accounts."),
            @Tag(
                    name = TAG_REPORTS,
                    description =
                            "API for generating and retrieving financial reports and analytics."),
            @Tag(
                    name = TAG_TRANSACTION,
                    description =
                            "API for creating, updating, and managing individual financial"
                                    + " transactions."),
            @Tag(
                    name = TAG_TRANSACTION_IMPORT,
                    description =
                            "API for importing transactions from external sources and file"
                                    + " formats."),
            @Tag(
                    name = TAG_TRANSACTION_ANALYTICS,
                    description =
                            "API for retrieving analytical data and insights about transaction"
                                    + " patterns."),
            @Tag(
                    name = TAG_TRANSACTION_TAGGING,
                    description =
                            "API for managing tags and labels associated with transactions for"
                                    + " better organization."),
            @Tag(
                    name = TAG_AUTOMATION_RULES,
                    description =
                            "API for defining and managing rules that automate transaction"
                                    + " processing and categorization."),
            @Tag(
                    name = TAG_AUTOMATION_PROCESSES,
                    description =
                            "API for managing automated business processes and workflows related to"
                                    + " financial data."),
            @Tag(
                    name = TAG_SETTINGS,
                    description =
                            "API for configuring general application settings and preferences."),
            @Tag(
                    name = TAG_SETTINGS_CURRENCIES,
                    description =
                            "API for managing currency settings, exchange rates, and"
                                    + " currency-related configurations."),
            @Tag(
                    name = TAG_SETTINGS_LOCALIZATION,
                    description = "API for configuring language, region, and format preferences."),
            @Tag(
                    name = TAG_SECURITY,
                    description =
                            "API for managing security settings, permissions, and access"
                                    + " controls."),
            @Tag(
                    name = TAG_SECURITY_USERS,
                    description =
                            "API for user management, including creation, updates, and permission"
                                    + " assignments."),
        })
@SecurityScheme(
        name = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer")
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
        System.exit(0);
    }
}
