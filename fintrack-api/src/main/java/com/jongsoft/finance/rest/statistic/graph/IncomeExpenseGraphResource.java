package com.jongsoft.finance.rest.statistic.graph;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;

import javax.annotation.Resource;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.core.date.Dates;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.filter.RequestAttributes;
import com.jongsoft.finance.math.MovingAverage;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.highchart.Highchart;
import com.jongsoft.highchart.axis.Axis;
import com.jongsoft.highchart.axis.AxisType;
import com.jongsoft.highchart.common.DashStyle;
import com.jongsoft.highchart.common.GraphColor;
import com.jongsoft.highchart.common.SeriesType;
import com.jongsoft.highchart.series.BarSeries;
import com.jongsoft.highchart.series.LineSeries;
import com.jongsoft.highchart.series.SeriesFactory;
import com.jongsoft.highchart.series.SeriesPoint;
import com.jongsoft.lang.API;

import io.micronaut.context.MessageSource;
import io.micronaut.http.HttpHeaders;
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

@Tag(name = "Reports")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/statistics/graph/income-expenses/{from}/{until}")
public class IncomeExpenseGraphResource {

    @Resource(name = "messageSource")
    private MessageSource messageSource;

    private final FilterFactory filterFactory;
    private final CurrentUserProvider currentUserProvider;
    private final TransactionProvider transactionProvider;

    public IncomeExpenseGraphResource(
            FilterFactory filterFactory,
            CurrentUserProvider currentUserProvider,
            TransactionProvider transactionProvider) {
        this.filterFactory = filterFactory;
        this.currentUserProvider = currentUserProvider;
        this.transactionProvider = transactionProvider;
    }

    @Get
    @Operation(
            operationId = "reportIncomeExpenseGraph",
            summary = "Generate a graph JSON for income / expense",
            description = "This operation will generate an income vs. expense graph for the provided date range",
            parameters = {
                    @Parameter(name = "from", in = ParameterIn.PATH, schema = @Schema(implementation = String.class)),
                    @Parameter(name = "until", in = ParameterIn.PATH, schema = @Schema(implementation = String.class)),
                    @Parameter(name = HttpHeaders.ACCEPT_LANGUAGE, in = ParameterIn.HEADER, required = true, example = "en"),
                    @Parameter(name = "X-Accept-Currency", in = ParameterIn.HEADER, required = true, example = "USD")
            }
    )
    Highchart graph(
            @PathVariable LocalDate from,
            @PathVariable LocalDate until,
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale,
            @RequestAttribute(RequestAttributes.CURRENCY) Currency currency) {
        var income = SeriesFactory.<BarSeries>createSeries(SeriesType.COLUMN);
        var expenses = SeriesFactory.<BarSeries>createSeries(SeriesType.COLUMN);
        var movingAverageExpense = SeriesFactory.<LineSeries>createSeries(SeriesType.LINE);
        var movingAverageIncome = SeriesFactory.<LineSeries>createSeries(SeriesType.LINE);

        var messageContext = MessageSource.MessageContext.of(locale);

        expenses.setName(messageSource.getMessage("graph.series.expenses", messageContext).get())
                .setId("expenses")
                .setColor(new GraphColor("#dc3545"));
        movingAverageExpense
                .setDashStyle(DashStyle.LONG_DASH)
                .setName(messageSource.getMessage("graph.series.expenses.sma", messageContext).get());
        movingAverageIncome
                .setDashStyle(DashStyle.LONG_DASH)
                .setName(messageSource.getMessage("graph.series.income.sma", messageContext).get());
        income.setName(messageSource.getMessage("graph.series.income", messageContext).get())
                .setId("income")
                .setColor(new GraphColor("#7fc6a5"));

        var currentStart = from;
        var currencyCode = API.Option(currency).map(Currency::getCode).getOrSupply(() -> "EUR");
        var currentEnd = Dates.endOfMonth(currentStart.getYear(), currentStart.getMonthValue());
        var avgExpense = new MovingAverage(4);
        var avgIncome = new MovingAverage(4);

        while (currentStart.isBefore(until)) {
            var earnings = transactionProvider.balance(filterFactory.transaction()
                    .ownAccounts()
                    .onlyIncome(true)
                    .currency(currencyCode)
                    .range(DateRange.of(currentStart, currentEnd)))
                    .getOrSupply(() -> 0D);

            var expense = transactionProvider.balance(filterFactory.transaction()
                    .ownAccounts()
                    .onlyIncome(false)
                    .currency(currencyCode)
                    .range(DateRange.of(currentStart, currentEnd)))
                    .getOrSupply(() -> 0D);

            avgExpense.add(BigDecimal.valueOf(expense).abs());
            avgIncome.add(BigDecimal.valueOf(earnings).abs());

            var timestamp = currentStart.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
            income.addPoint(new SeriesPoint()
                    .setY(earnings)
                    .setX(timestamp));
            expenses.addPoint(new SeriesPoint()
                    .setY(Math.abs(expense))
                    .setX(timestamp));
            movingAverageExpense.addPoint(new SeriesPoint()
                    .setY(avgExpense.getAverage())
                    .setX(timestamp));
            movingAverageIncome.addPoint(new SeriesPoint()
                    .setY(avgIncome.getAverage())
                    .setX(timestamp));

            currentStart = currentStart.plusMonths(1);
            currentEnd = Dates.endOfMonth(currentStart.getYear(), currentStart.getMonthValue());
        }

        // @formatter:off
        return new Highchart()
                .addXAxis(new Axis().setType(AxisType.DATETIME))
                .addYAxis(createSpendingAxis(locale))
                .addSeries(expenses)
                .addSeries(income)
                .addSeries(movingAverageExpense)
                .addSeries(movingAverageIncome)
                .getChart()
                    .setType(null)
                    .build()
                .getTitle()
                    .setText("")
                    .build()
                .getChart()
                    .setType(SeriesType.COLUMN)
                    .build()
                .getTooltip()
                    .setShared(false)
                    .setValueDecimals(2)
                    .setHeaderFormat("<strong>{series.name}</strong><br />")
                    .setPointFormat(getCurrency(locale) + "{point.y}")
                    .build()
                .getLegend()
                    .setEnabled(false)
                    .build()
                .getCredits()
                    .setText("FinTrack")
                    .setEnabled(false)
                    .build();
        // @formatter:on
    }

    private Axis createSpendingAxis(Locale locale) {
        // @formatter:off
        return new Axis()
                .setType(AxisType.LINEAR)
                .getTitle()
                    .setText("")
                    .build()
                .setOpposite(false)
                .setSoftMax(0)
                .setMin(0)
                .getLabels()
                    .setFormat(getCurrency(locale) + "{value}")
                    .build();
        // @formatter:on
    }

    private String getCurrency(Locale locale) {
        return currentUserProvider.currentUser()
                .getPrimaryCurrency()
                .getSymbol(locale);
    }
}
