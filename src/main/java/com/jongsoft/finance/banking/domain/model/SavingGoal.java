package com.jongsoft.finance.banking.domain.model;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.banking.domain.commands.*;
import com.jongsoft.finance.core.value.Periodicity;
import com.jongsoft.finance.core.value.Schedulable;
import com.jongsoft.finance.core.value.Schedule;
import com.jongsoft.lang.Control;

import io.micronaut.core.annotation.Introspected;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

@Introspected
public class SavingGoal {

    private Long id;
    private String name;
    private String description;
    private BigDecimal allocated;
    private BigDecimal goal;
    private LocalDate targetDate;
    private Account account;
    private Schedule schedule;

    private SavingGoal(Account account, String name, BigDecimal goal, LocalDate targetDate) {
        if (!account.isManaged()) {
            throw StatusException.badRequest(
                    "Cannot create a savings goal if the account is not owned by the user.");
        }

        this.account = Objects.requireNonNull(account, "Account cannot be empty.");
        this.goal = goal;
        this.targetDate = targetDate;
        this.name = name;

        CreateSavingGoalCommand.savingGoalCreated(account.getId(), name, goal, targetDate);
    }

    // Used by the Mapper
    SavingGoal(
            Long id,
            String name,
            String description,
            BigDecimal allocated,
            BigDecimal goal,
            LocalDate targetDate,
            Account account,
            Schedule schedule) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.allocated = allocated;
        this.goal = goal;
        this.targetDate = targetDate;
        this.account = account;
        this.schedule = schedule;
    }

    /**
     * Call this operation to calculate the amount of money that should be allocated to this saving
     * goal every schedule step to reach the goal.
     *
     * <p>Note: since this is a calculation the value can vary, based upon the already allocated
     * amount of money in the account.
     *
     * @return the amount the user should set apart when the next saving date comes along
     */
    public BigDecimal computeAllocation() {
        var remainingToGoal =
                goal.subtract(Control.Option(allocated).getOrSupply(() -> BigDecimal.ZERO));

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

        return remainingToGoal.divide(BigDecimal.valueOf(times), RoundingMode.HALF_UP);
    }

    /**
     * Change either the targeted amount of money that should be reserved or the date at which is
     * should be available.
     *
     * @param goal the target amount of money
     * @param targetDate the date at which the goal should be met
     */
    public void adjustGoal(BigDecimal goal, LocalDate targetDate) {
        if (LocalDate.now().isAfter(targetDate)) {
            throw StatusException.badRequest(
                    "Target date for a saving goal cannot be in the past.");
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
     * @param interval the interval to recur on
     */
    public void schedule(Periodicity periodicity, int interval) {
        var firstSaving = LocalDate.now().plus(interval, periodicity.toChronoUnit());
        if (firstSaving.isAfter(targetDate)) {
            throw StatusException.badRequest(
                    "Cannot set schedule when first saving would be after the target date of this"
                            + " saving goal.");
        }

        this.schedule = new ScheduleValue(periodicity, interval);
        AdjustScheduleCommand.scheduleAdjusted(
                id, Schedulable.basicSchedule(this.id, this.targetDate, this.schedule));
    }

    /**
     * Calling this method will create the next installment towards the end goal. The installment is
     * calculated using the {@link #computeAllocation()} method.
     *
     * @throws StatusException in case no schedule was yet activated on this saving goal
     */
    public void reserveNextPayment() {
        if (schedule == null) {
            throw StatusException.badRequest(
                    "Cannot automatically reserve an installment for saving goal "
                            + id
                            + ". No schedule was setup.");
        }

        var installment = computeAllocation();
        if (installment.compareTo(BigDecimal.ZERO) > 0) {
            this.allocated = this.allocated.add(installment);
            RegisterSavingInstallmentCommand.savingInstallmentRegistered(id, installment);
        }
    }

    /**
     * Add additional money towards the savings goal. This does not require any scheduling (for
     * automated savings).
     *
     * @param amount the amount to add
     * @throws StatusException in case the saved amount exceeds the targeted goal amount
     */
    public void registerPayment(BigDecimal amount) {
        if (allocated.add(amount).compareTo(goal) > 0) {
            throw StatusException.badRequest(
                    "Cannot increase allocation, the increment would add more then the desired goal"
                            + " of "
                            + goal);
        }

        this.allocated = this.allocated.add(amount);
        RegisterSavingInstallmentCommand.savingInstallmentRegistered(id, amount);
    }

    /** Signal that the savings goal has been used for its intended purpose. */
    public void completed() {
        CompleteSavingGoalCommand.savingGoalCompleted(id);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAllocated() {
        return allocated;
    }

    public BigDecimal getGoal() {
        return goal;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public Account getAccount() {
        return account;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public static SavingGoal create(
            Account account, String name, BigDecimal goal, LocalDate targetDate) {
        return new SavingGoal(account, name, goal, targetDate);
    }
}
