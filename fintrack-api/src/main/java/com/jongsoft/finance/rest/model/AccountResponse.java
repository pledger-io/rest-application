package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.SavingGoal;
import com.jongsoft.finance.schedule.Periodicity;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Set;

@Serdeable.Serializable
public class AccountResponse {

    private final Account wrapped;

    public AccountResponse(final Account wrapped) {
        Objects.requireNonNull(wrapped, "Account cannot be null for JSON response.");
        this.wrapped = wrapped;
    }

    @Schema(description = "The identifier of the account", example = "3212", required = true)
    public long getId() {
        return wrapped.getId();
    }

    @Schema(
            description = "The account name, is unique for the user",
            example = "Fast food & co",
            required = true)
    public String getName() {
        return wrapped.getName();
    }

    @Schema(description = "The description for the account")
    public String getDescription() {
        return wrapped.getDescription();
    }

    @Schema(
            description = "The type of account, as defined by the account type API",
            example = "creditor",
            required = true)
    public String getType() {
        return wrapped.getType();
    }

    @Schema(description = "The file code for the image of the account")
    public String getIconFileCode() {
        return wrapped.getImageFileToken();
    }

    @Schema(description = "Bank identification numbers for the account", required = true)
    public NumberInformation getAccount() {
        return new NumberInformation();
    }

    @Schema(
            description =
                    "The interest information for the account, only used for loans, debts and"
                            + " mortgage")
    public InterestInformation getInterest() {
        return new InterestInformation();
    }

    @Schema(description = "Transaction history information for the account")
    public History getHistory() {
        return new History();
    }

    @Schema(
            description =
                    "The saving goals for the account, only valid for type savings and"
                            + " joined_savings")
    public Set<SavingGoalResponse> getSavingGoals() {
        if (wrapped.getSavingGoals() != null) {
            return wrapped.getSavingGoals().map(SavingGoalResponse::new).toJava();
        }

        return null;
    }

    @Serdeable.Serializable
    public static class SavingGoalResponse {

        private final SavingGoal wrapped;

        public SavingGoalResponse(SavingGoal wrapped) {
            this.wrapped = wrapped;
        }

        @Schema(description = "The identifier of the saving goal", example = "132", required = true)
        public long getId() {
            return wrapped.getId();
        }

        @Schema(description = "The name of the saving goal", example = "Car replacement")
        public String getName() {
            return wrapped.getName();
        }

        @Schema(description = "The description of the saving goal")
        public String getDescription() {
            return wrapped.getDescription();
        }

        @Schema(description = "The schedule that allocations are created automatically")
        public ScheduleResponse getSchedule() {
            if (wrapped.getSchedule() != null) {
                return new ScheduleResponse(wrapped.getSchedule());
            }

            return null;
        }

        @Schema(
                description = "The goal one wishes to achieve by the end date",
                type = "number",
                example = "1500.40",
                required = true)
        public BigDecimal getGoal() {
            return wrapped.getGoal();
        }

        @Schema(
                description = "The amount of money reserved for this saving goal",
                type = "number",
                example = "200",
                required = true)
        public BigDecimal getReserved() {
            return wrapped.getAllocated();
        }

        @Schema(
                description =
                        "The amount of money allocated each interval, only when schedule is set",
                type = "number",
                example = "25.50")
        public BigDecimal getInstallments() {
            if (wrapped.getSchedule() != null) {
                return wrapped.computeAllocation();
            }

            return null;
        }

        @Schema(
                description = "The date before which the goal must be met",
                example = "2021-01-12",
                required = true)
        public LocalDate getTargetDate() {
            return wrapped.getTargetDate();
        }

        @Schema(description = "The amount of months left until the target date", example = "23")
        public long getMonthsLeft() {
            var monthsLeft = ChronoUnit.MONTHS.between(LocalDate.now(), wrapped.getTargetDate());
            return Math.max(0, monthsLeft);
        }
    }

    @Serdeable.Serializable
    public class InterestInformation {

        @Schema(description = "The interval the interest is calculated on", example = "MONTHS")
        public Periodicity getPeriodicity() {
            return wrapped.getInterestPeriodicity();
        }

        @Schema(description = "The amount of interest that is owed", example = "0.0754")
        public double getInterest() {
            return wrapped.getInterest();
        }
    }

    @Serdeable.Serializable
    public class NumberInformation {

        public String getIban() {
            return wrapped.getIban();
        }

        public String getBic() {
            return wrapped.getBic();
        }

        public String getNumber() {
            return wrapped.getNumber();
        }

        public String getCurrency() {
            return wrapped.getCurrency();
        }
    }

    @Serdeable.Serializable
    public class History {

        @Schema(description = "The date of the first recorded transaction for the account")
        public LocalDate getFirstTransaction() {
            return wrapped.getFirstTransaction();
        }

        @Schema(description = "The date of the latest recorded transaction for the account")
        public LocalDate getLastTransaction() {
            return wrapped.getLastTransaction();
        }
    }
}
