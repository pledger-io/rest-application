package com.jongsoft.finance.rest.budget.graph;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.core.date.Dates;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.filter.RequestAttributes;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.highchart.Highchart;
import com.jongsoft.highchart.axis.Axis;
import com.jongsoft.highchart.axis.AxisType;
import com.jongsoft.highchart.common.GraphColor;
import com.jongsoft.highchart.common.SeriesType;
import com.jongsoft.highchart.series.LineSeries;
import com.jongsoft.highchart.series.SeriesFactory;
import com.jongsoft.highchart.series.SeriesPoint;
import io.micronaut.context.MessageSource;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.RequestAttribute;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.function.Function;

@Tag(name = "Graph Generation")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/budgets/{year}/graph")
public class BudgetYearGraphResource {

    private final FilterFactory filterFactory;
    private final BudgetProvider budgetProvider;
    private final TransactionProvider transactionProvider;
    private final CurrentUserProvider currentUserProvider;
    private final MessageSource messageSource;

    public BudgetYearGraphResource(
            FilterFactory filterFactory,
            BudgetProvider budgetProvider,
            TransactionProvider transactionProvider,
            CurrentUserProvider currentUserProvider,
            MessageSource messageSource) {
        this.filterFactory = filterFactory;
        this.budgetProvider = budgetProvider;
        this.transactionProvider = transactionProvider;
        this.currentUserProvider = currentUserProvider;
        this.messageSource = messageSource;
    }

    @Get("/expenses")
    @Operation(
            summary = "Budget Yearly Expense Graph",
            description = "Generate a yearly budget expense graph",
            parameters = @Parameter(name = "year", in = ParameterIn.PATH, schema = @Schema(implementation = Integer.class))
    )
    String expense(
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale,
            @PathVariable int year) {
        Function<Budget, TransactionProvider.FilterCommand> request =
                b -> filterFactory.transaction()
                        .expenses(b.getExpenses().map(e -> new EntityRef(e.getId())))
                        .onlyIncome(false);

        return createChart(locale, year, new GraphColor("#de7370"), request, Budget::computeExpenses)
                .toJson();
    }

    @Get("/income")
    @Operation(
            summary = "Budget Yearly Income Graph",
            description = "Generate a yearly budget income graph",
            parameters = @Parameter(name = "year", in = ParameterIn.PATH, schema = @Schema(implementation = Integer.class))
    )
    String income(
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale,
            @PathVariable int year) {
        Function<Budget, TransactionProvider.FilterCommand> request =
                b -> filterFactory.transaction().onlyIncome(true);

        return createChart(locale, year, new GraphColor("#6996b2"), request, Budget::getExpectedIncome)
                .toJson();
    }

    private Highchart createChart(Locale locale, int year, GraphColor graphColor,
                                  Function<Budget, TransactionProvider.FilterCommand> request,
                                  Function<Budget, Double> expectation) {
        LineSeries actualSeries = SeriesFactory.createSeries(SeriesType.LINE);
        LineSeries budgetSeries = SeriesFactory.createSeries(SeriesType.LINE);

        budgetSeries.setColor(new GraphColor("#f0c77c"))
                .setName(messageSource.getMessage(
                        "graph.series.budget.expected",
                        MessageSource.MessageContext.of(locale)).get());
        actualSeries.setColor(graphColor)
                .setName(messageSource.getMessage(
                        "graph.series.budget.actual",
                        MessageSource.MessageContext.of(locale)).get());

        LocalDate currentStart = LocalDate.of(year, 1, 1);
        LocalDate currentEnd = Dates.endOfMonth(currentStart.getYear(), currentStart.getMonthValue());

        LocalDate totalEnd = LocalDate.of(year, 12, 31);
        while (currentStart.isBefore(totalEnd)) {
            var budget = budgetProvider.lookup(currentStart.getYear(), currentStart.getMonthValue()).blockingGet();

            double expected = expectation.apply(budget);
            double actual = transactionProvider.balance(request.apply(budget)
                    .range(DateRange.of(currentStart, currentEnd))
                    .ownAccounts())
                    .getOrSupply(() -> 0D);

            final long timestamp = Dates.timestamp(currentStart);
            budgetSeries.addPoint(new SeriesPoint()
                    .setY(expected)
                    .setX(timestamp));

            actualSeries.addPoint(new SeriesPoint()
                    .setY(Math.abs(actual))
                    .setX(timestamp));

            currentStart = currentStart.plusMonths(1);
            currentEnd = Dates.endOfMonth(currentStart.getYear(), currentStart.getMonthValue());
        }

        // @formatter:off
        Highchart chart = new Highchart()
                .getTitle()
                    .setText("")
                    .build()
                .getChart()
                    .setType(SeriesType.LINE)
                    .setHeight(350)
                    .build()
                .getCredits()
                    .setEnabled(false)
                    .setText("FinTrack")
                    .build()
                .getLegend()
                    .setEnabled(false)
                    .build()
                .getTooltip()
                    .setValueDecimals(2)
                    .setPointFormat(getCurrency(locale) + "{point.y}")
                    .build()
                .getLegend()
                    .setEnabled(true)
                    .build()
                .addXAxis(createDateAxis(
                        Dates.toDate(LocalDate.of(year, 1, 1)),
                        Dates.toDate(LocalDate.of(year, 12, 31))))
                .addYAxis(createBalanceAxis(locale))
                .addSeries(budgetSeries)
                .addSeries(actualSeries);
        // @formatter:on

        chart.getPlotOptions().getSeries().getMarker().setEnabled(false);
        return chart;
    }

    private Axis createDateAxis(Date start, Date end) {
        // @formatter:off
        return new Axis()
                .getTitle()
                    .setText("")
                    .build()
                .setMin(start.getTime())
                .setMax(end.getTime())
                .setMinTickInterval(3600000 * 24)
                .setType(AxisType.DATETIME);
        // @formatter:on
    }

    private Axis createBalanceAxis(Locale locale) {
        // @formatter:off
        return new Axis()
                .getTitle()
                    .setText("")
                    .build()
                .setSoftMax(0)
                .setSoftMin(0)
                .setAllowDecimals(true)
                .getLabels()
                    .setFormat(getCurrency(locale) + " {value}")
                    .build()
                .setType(AxisType.LINEAR);
        // @formatter:on
    }

    private String getCurrency(Locale locale) {
        Currency currency = currentUserProvider.currentUser().getPrimaryCurrency();
        if (currency == null) {
            currency = Currency.getInstance("EUR");
        }

        return currency.getSymbol(locale);
    }
}
