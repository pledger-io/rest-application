package com.jongsoft.finance.graph;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.highchart.Highchart;
import com.jongsoft.highchart.common.GraphColor;
import com.jongsoft.highchart.common.SeriesType;
import com.jongsoft.highchart.series.PieSeries;
import com.jongsoft.highchart.series.SeriesFactory;
import com.jongsoft.highchart.series.SeriesPoint;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.time.Range;
import io.micronaut.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

public abstract class BudgetPieChart {

    private final MessageSource messageSource;
    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;
    private final BudgetProvider budgetProvider;

    protected BudgetPieChart(
            MessageSource messageSource,
            FilterFactory filterFactory,
            TransactionProvider transactionProvider,
            BudgetProvider budgetProvider) {
        this.messageSource = messageSource;
        this.filterFactory = filterFactory;
        this.transactionProvider = transactionProvider;
        this.budgetProvider = budgetProvider;
    }

    protected Highchart createChart(String currencySymbol, Locale locale) {
        // @formatter:off
        return new Highchart().getTitle()
                    .setText("")
                    .build()
                .getChart()
                    .setType(SeriesType.PIE)
                    .setHeight(300)
                    .setBackgroundColor(new GraphColor("transparent"))
                    .build()
                .getTooltip()
                    .setShared(false)
                    .setValueDecimals(2)
                    .setPointFormat(currencySymbol + "{point.y}")
                    .build()
                .getCredits()
                    .setEnabled(false)
                    .setText("FinTrack")
                    .build();
        // @formatter:on
    }

    protected PieSeries createSeries(Sequence<Account> accounts, LocalDate start, LocalDate end, Locale locale) {
        PieSeries series = SeriesFactory.createSeries(SeriesType.PIE);

        Range<LocalDate> dateRange = Dates.range(start, end);
        Budget budget = budgetProvider.lookup(start.getYear(), start.getMonthValue())
                .blockingGet();

        if (budget != null) {
            var request = filterFactory.transaction()
                    .range(dateRange)
                    .accounts(accounts.map(account -> new EntityRef(account.getId())))
                    .onlyIncome(false);

            var totalBalance = BigDecimal.valueOf(transactionProvider.balance(request).getOrSupply(() -> 0D));
            for (Budget.Expense expense : budget.getExpenses()) {
                double balance = transactionProvider.balance(
                        request.expenses(
                                Collections.List(new EntityRef(expense.getId()))))
                        .getOrSupply(() -> 0D);

                totalBalance = totalBalance.subtract(BigDecimal.valueOf(balance));
                SeriesPoint point = new SeriesPoint()
                        .setName(expense.getName())
                        .setY(Math.abs(balance));
                series.addPoint(point);
            }

            series.addPoint(new SeriesPoint()
                    .setName(messageSource.getMessage("Budget.none", MessageSource.MessageContext.of(locale)).get())
                    .setY(Math.abs(totalBalance.doubleValue())));
        }

        series.setName(messageSource.getMessage("graph.series.budget", MessageSource.MessageContext.of(locale)).get());
        series.getDataLabels().setEnabled(false);
        return series;
    }
}
