package com.jongsoft.finance.domain.account;

import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.messaging.commands.savings.*;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.schedule.Schedulable;
import com.jongsoft.finance.schedule.Schedule;
import com.jongsoft.lang.Control;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SavingGoal implements AggregateBase {

    private Long id;
    private String name;
    private String description;
    private BigDecimal allocated;
    private BigDecimal goal;
    private LocalDate targetDate;
    private Account account;
    private Schedule schedule;

    SavingGoal(Account account, String name, BigDecimal goal, LocalDate targetDate) {
        if (!account.isManaged()) {
            throw StatusException.badRequest("Cannot create a savings goal if the account is not owned by the user.");
        }

        this.account = Objects.requireNonNull(account, "Account cannot be empty.");
        this.goal = goal;
        this.targetDate = targetDate;
        this.name = name;

        CreateSavingGoalCommand.savingGoalCreated(account.getId(), name, goal, targetDate);
    }

    /**
     * Call this operation to calculate the amount of money that should be allocated to this saving goal every
     * schedule step to reach the goal.
     * <p>
     * Note: since this is a calculation the value can vary, based upon the already allocated amount of money in the
     * account.
     *
     * @return the amount the user should set apart when the next saving date comes along
     */
    public BigDecimal computeAllocation() {
        var remainingToGoal = goal.subtract(
                Control.Option(allocated)
                        .getOrSupply(() -> BigDecimal.ZERO));

        if (remainingToGoal.compareTo(BigDecimal.ONE) < 0) {
            return BigDecimal.ZERO;
        }

        var times = 0;
        var now = LocalDate.now();
        var allocationTime = schedule.previous(targetDate);
        while (now.isBefore(allocationTime)) {
            times++;
            allocationTime = schedule.previous(allocationTime);
        }

        return remainingToGoal.divide(
                BigDecimal.valueOf(times),
                RoundingMode.HALF_UP);
    }

    /**
     * Change either the targeted amount of money that should be reserved or the date at which is should be
     * available.
     *
     * @param goal       the target amount of money
     * @param targetDate the date at which the goal should be met
     */
    @BusinessMethod
    public void adjustGoal(BigDecimal goal, LocalDate targetDate) {
        if (LocalDate.now().isAfter(targetDate)) {
            throw StatusException.badRequest("Target date for a saving goal cannot be in the past.");
        } else if (BigDecimal.ZERO.compareTo(goal) >= 0) {
            throw StatusException.badRequest("The goal cannot be 0 or less.");
        }

        this.goal = goal;
        this.targetDate = targetDate;
        AdjustSavingGoalCommand.savingGoalAdjusted(id, goal, targetDate);
    }

    /**
     * Set the interval at which one wishes to add money into the saving goal.
     *
     * @param periodicity the periodicity
     * @param interval    the interval to recur on
     */
    @BusinessMethod
    public void schedule(Periodicity periodicity, int interval) {
        var firstSaving = LocalDate.now().plus(interval, periodicity.toChronoUnit());
        if (firstSaving.isAfter(targetDate)) {
            throw StatusException.badRequest(
                    "Cannot set schedule when first saving would be after the target date of this saving goal.");
        }

        this.schedule = new ScheduleValue(periodicity, interval);
        AdjustScheduleCommand.scheduleAdjusted(id, Schedulable.basicSchedule(
                this.id,
                this.targetDate,
                this.schedule));
    }

    /**
     * Calling this method will create the next installment towards the end goal. The installment is calculated using
     * the {@link #computeAllocation()} method.
     *
     * @throws StatusException in case no schedule was yet activated on this saving goal
     */
    @BusinessMethod
    public void reserveNextPayment() {
        if (schedule == null) {
            throw StatusException.badRequest(
                    "Cannot automatically reserve an installment for saving goal " + id + ". No schedule was setup.");
        }

        var installment = computeAllocation();
        if (installment.compareTo(BigDecimal.ZERO) > 0) {
            this.allocated = this.allocated.add(installment);
            RegisterSavingInstallmentCommand.savingInstallmentRegistered(id, installment);
        }
    }

    /**
     * Add additional money towards the savings goal. This does not require any scheduling (for automated savings).
     *
     * @param amount the amount to add
     * @throws StatusException in case the saved amount exceeds the targeted goal amount
     */
    @BusinessMethod
    public void registerPayment(BigDecimal amount) {
        if (allocated.add(amount).compareTo(goal) > 0) {
            throw StatusException.badRequest("Cannot increase allocation, the increment would add more then the desired goal of " + goal);
        }

        this.allocated = this.allocated.add(amount);
        RegisterSavingInstallmentCommand.savingInstallmentRegistered(id, amount);
    }

    /**
     * Signal that the savings goal has been used for its intended purpose.
     */
    @BusinessMethod
    public void completed() {
        CompleteSavingGoalCommand.savingGoalCompleted(id);
    }

}
