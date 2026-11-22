package com.jongsoft.finance.health.domain.jpa;

import com.jongsoft.finance.health.domain.jpa.entity.HealthSnapshotJpa;
import com.jongsoft.finance.health.domain.model.HealthComponent;
import com.jongsoft.finance.health.domain.model.HealthSnapshot;
import com.jongsoft.finance.health.types.UnavailableReason;

import io.micronaut.context.annotation.Mapper;

import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Singleton
public interface HealthSnapshotMapper {

    double SAVINGS_WEIGHT = 0.35D;
    double DEBT_TO_INCOME_WEIGHT = 0.35D;
    double BUDGET_WEIGHT = 0.30D;

    @Mapper.Mapping(to = "period", from = "yearMonth")
    @Mapper.Mapping(to = "savingsRate", from = "#{this.savingsComponent(entity)}")
    @Mapper.Mapping(to = "debtToIncome", from = "#{this.debtToIncomeComponent(entity)}")
    @Mapper.Mapping(to = "budgetAdherence", from = "#{this.budgetComponent(entity)}")
    HealthSnapshot convertToDomain(HealthSnapshotJpa entity);

    default HealthComponent savingsComponent(HealthSnapshotJpa entity) {
        if (entity.getSavingsScore() == null) {
            return unavailable(UnavailableReason.NO_INCOME);
        }
        return new HealthComponent(
                true,
                null,
                entity.getSavingsRate().doubleValue(),
                entity.getSavingsScore(),
                effectiveWeight(SAVINGS_WEIGHT, entity),
                details("income", entity.getIncome(), "expenses", entity.getExpenses()));
    }

    default HealthComponent debtToIncomeComponent(HealthSnapshotJpa entity) {
        if (entity.getDtiScore() == null) {
            return unavailable(UnavailableReason.NO_INCOME);
        }
        return new HealthComponent(
                true,
                null,
                entity.getDebtToIncome().doubleValue(),
                entity.getDtiScore(),
                effectiveWeight(DEBT_TO_INCOME_WEIGHT, entity),
                details("income", entity.getIncome(), "debtPayments", entity.getDebtPayments()));
    }

    default HealthComponent budgetComponent(HealthSnapshotJpa entity) {
        if (entity.getBudgetScore() == null) {
            return unavailable(UnavailableReason.NO_BUDGET);
        }
        return new HealthComponent(
                true,
                null,
                entity.getBudgetAdherence().doubleValue(),
                entity.getBudgetScore(),
                effectiveWeight(BUDGET_WEIGHT, entity),
                Map.of());
    }

    default double effectiveWeight(double baseWeight, HealthSnapshotJpa entity) {
        double availableWeight = 0D;
        if (entity.getSavingsScore() != null) {
            availableWeight += SAVINGS_WEIGHT;
        }
        if (entity.getDtiScore() != null) {
            availableWeight += DEBT_TO_INCOME_WEIGHT;
        }
        if (entity.getBudgetScore() != null) {
            availableWeight += BUDGET_WEIGHT;
        }
        return availableWeight == 0D ? 0D : baseWeight / availableWeight;
    }

    private HealthComponent unavailable(UnavailableReason reason) {
        return new HealthComponent(false, reason, 0D, 0, 0D, Map.of());
    }

    private Map<String, Double> details(Object... keyValues) {
        Map<String, Double> details = new HashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            details.put(
                    keyValues[i].toString(),
                    switch (keyValues[i + 1]) {
                        case Double value -> value;
                        case BigDecimal value -> value.doubleValue();
                        default -> 0D;
                    });
        }
        return details;
    }
}
