package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.tag.CreateTagCommand;
import com.jongsoft.finance.messaging.commands.user.ChangeMultiFactorCommand;
import com.jongsoft.finance.messaging.commands.user.ChangePasswordCommand;
import com.jongsoft.finance.messaging.commands.user.ChangeUserSettingCommand;
import com.jongsoft.finance.messaging.commands.user.CreateUserCommand;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.collection.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

@Getter
@Builder
@Aggregate
@AllArgsConstructor
@ToString(of = "username")
public class UserAccount implements AggregateBase, Serializable {

    private Long id;
    private String username;
    private String password;
    private List<Role> roles;

    private String theme;
    private Currency primaryCurrency;
    private transient String profilePicture;
    private String secret;
    private boolean twoFactorEnabled;

    public UserAccount(String username, String password) {
        this.username = username;
        this.password = password;
        this.roles = Collections.List(new Role("accountant"));
        EventBus.getBus().send(new CreateUserCommand(this.username, this.password));
    }

    /**
     * Change the password of the user to the provided new password.
     *
     * @param password  the new password
     */
    @BusinessMethod
    public void changePassword(String password) {
        this.password = password;
        EventBus.getBus().send(new ChangePasswordCommand(username, this.password));
    }

    /**
     * Change the currency of the account to the desired one. This is the default currency used throughout
     * the application.
     *
     * @param currency  the new default currency
     */
    @BusinessMethod
    public void changeCurrency(Currency currency) {
        if (!Objects.equals(this.primaryCurrency, currency)) {
            this.primaryCurrency = currency;
            EventBus.getBus().send(new ChangeUserSettingCommand(
                    this.username,
                    ChangeUserSettingCommand.Type.CURRENCY,
                    this.primaryCurrency.getCurrencyCode()));
        }
    }

    /**
     * Change the theme selected by the user.
     *
     * @param theme the newly selected theme
     */
    @BusinessMethod
    public void changeTheme(String theme) {
        if (!Objects.equals(this.theme, theme)) {
            this.theme = theme;
            EventBus.getBus().send(new ChangeUserSettingCommand(
                    this.username,
                    ChangeUserSettingCommand.Type.THEME,
                    this.theme));
        }
    }

    /**
     * Enable multi factor authentication for the current user.
     */
    @BusinessMethod
    public void enableMultiFactorAuthentication() {
        if (!twoFactorEnabled) {
            this.twoFactorEnabled = true;
            EventBus.getBus().send(new ChangeMultiFactorCommand(username, true));
        }
    }

    /**
     * Disable multi factor authentication for the current user.
     */
    @BusinessMethod
    public void disableMultiFactorAuthentication() {
        if (twoFactorEnabled) {
            this.twoFactorEnabled = false;
            EventBus.getBus().send(new ChangeMultiFactorCommand(username, false));
        }
    }

    /**
     * Create a new account for the current user.
     *
     * @param name      the name of the account
     * @param currency  the currency of the account
     * @param type      the account type
     * @return          the newly created account linked to this user
     */
    @BusinessMethod
    public Account createAccount(String name, String currency, String type) {
        if (notFullUser()) {
            throw StatusException.notAuthorized("User cannot create accounts, incorrect privileges.");
        }

        return new Account(this, name, currency, type);
    }

    /**
     * Creates a new category registered to the current user.
     *
     * @param label     the label of the category
     * @return          the newly created category
     */
    @BusinessMethod
    public Category createCategory(String label) {
        if (notFullUser()) {
            throw StatusException.notAuthorized("User cannot create categories, incorrect privileges.");
        }

        return new Category(this, label);
    }

    @BusinessMethod
    public Tag createTag(String label) {
        if (notFullUser()) {
            throw StatusException.notAuthorized("User cannot create tags, incorrect privileges.");
        }

        EventBus.getBus().send(new CreateTagCommand(label));
        return new Tag(label);
    }

    /**
     * Create a new transaction rule for the user.
     *
     * @param name          the name of the rule
     * @param restrictive   is the rule restrictive
     * @return              the newly created transaction rule
     */
    @BusinessMethod
    public TransactionRule createRule(String name, boolean restrictive) {
        if (notFullUser()) {
            throw StatusException.notAuthorized("User cannot create rules, incorrect privileges.");
        }

        return new TransactionRule(this, name, restrictive);
    }

    /**
     * Create a new import configuration for the current user.
     *
     * @param name      the name of the configuration
     * @return          the newly created configuration
     */
    @BusinessMethod
    public BatchImportConfig createImportConfiguration(String type, String name, String fileCode) {
        if (notFullUser()) {
            throw StatusException.notAuthorized("User cannot create import configuration, incorrect privileges.");
        }

        return new BatchImportConfig(this, type, name, fileCode);
    }

    /**
     * Create a new budget for the user.
     *
     * @param start             the start date of the budget
     * @param expectedIncome    the expected income
     * @return                  the newly created budget
     */
    @BusinessMethod
    public Budget createBudget(LocalDate start, double expectedIncome) {
        if (notFullUser()) {
            throw StatusException.notAuthorized("User cannot create budgets, incorrect privileges.");
        }

        var budget = new Budget(start, expectedIncome);
        budget.activate();
        return budget;
    }

    public boolean notFullUser() {
        return roles.stream()
                .noneMatch(r -> Objects.equals(r.getName(), "accountant") || Objects.equals(r.getName(), "admin"));
    }

}
