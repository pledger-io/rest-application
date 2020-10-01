package com.jongsoft.finance.rest.budget.graph;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.filter.RequestAttributes;
import com.jongsoft.highchart.Highchart;
import com.jongsoft.highchart.axis.Axis;
import com.jongsoft.highchart.axis.AxisType;
import com.jongsoft.highchart.common.GraphColor;
import com.jongsoft.highchart.common.SeriesType;
import com.jongsoft.highchart.series.ColumnSeries;
import com.jongsoft.highchart.series.SeriesFactory;
import com.jongsoft.highchart.series.SeriesPoint;
import com.jongsoft.lang.API;
import io.micronaut.context.MessageSource;
import io.micronaut.core.convert.format.Format;
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
import java.util.Locale;

@Tag(name = "Graph Generation")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/budgets/graph/{start}/{end}")
public class BudgetGraphResource {

    private final FilterFactory filterFactory;
    private final BudgetProvider budgetProvider;
    private final TransactionProvider transactionProvider;
    private final MessageSource messageSource;

    public BudgetGraphResource(
            FilterFactory filterFactory,
            BudgetProvider budgetService,
            TransactionProvider transactionService,
            MessageSource messageSource) {
        this.filterFactory = filterFactory;
        this.budgetProvider = budgetService;
        this.transactionProvider = transactionService;
        this.messageSource = messageSource;
    }

    @Get
    @Operation(
            summary = "Budget Month Graph",
            description = "Generate a month budget income graph",
            parameters = @Parameter(name = "year", in = ParameterIn.PATH, schema = @Schema(implementation = Integer.class))
    )
    Highchart graph(
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale,
            @PathVariable @Format("yyyy-MM-dd") LocalDate start,
            @PathVariable @Format("yyyy-MM-dd") LocalDate end) {
        final Axis xAxis = new Axis();
        final ColumnSeries series = (ColumnSeries) SeriesFactory.createSeries(SeriesType.COLUMN);
        final ColumnSeries expected = (ColumnSeries) SeriesFactory.createSeries(SeriesType.COLUMN);

        var budget = budgetProvider.lookup(start.getYear(), start.getMonthValue()).blockingGet();
        budget.getExpenses()
                .forEach(e -> {
                    double amount = transactionProvider.balance(filterFactory.transaction()
                            .expenses(API.List(new EntityRef(e.getId())))
                            .ownAccounts()
                            .range(DateRange.of(start, end))).getOrSupply(() -> 0D);
                    series.addPoint(new SeriesPoint()
                            .setName(e.getName())
                            .setY(Math.abs(amount)));

                    expected.addPoint(new SeriesPoint()
                            .setName(e.getName())
                            .setY(e.computeBudget()));

                    xAxis.addCategory(e.getName());
                });

        series.setGrouping(false)
                .setColor(new GraphColor("#7fc6a5"))
                .setPointPadding(0.2)
                .setName(messageSource.getMessage(
                        "graph.series.budget.actual",
                        MessageSource.MessageContext.of(locale)).get());
        expected.setGrouping(false)
                .setColor(new GraphColor("#9abdd2"))
                .setName(messageSource.getMessage(
                        "graph.series.budget.expected",
                        MessageSource.MessageContext.of(locale)).get());

        // @formatter:off
        return new Highchart()
                .getTitle()
                    .setText("")
                    .build()
                .getChart()
                    .setType(SeriesType.COLUMN)
                    .build()
                .getTooltip()
                    .setShared(true)
                    .build()
                .getLegend()
                    .setEnabled(false)
                    .build()
                .getCredits()
                    .setText("FinTrack")
                    .setEnabled(false)
                    .build()
                .addXAxis(xAxis)
                .addYAxis(createBalanceAxis(locale))
                .addSeries(expected)
                .addSeries(series);
        // @formatter:on
    }

    private Axis createBalanceAxis(Locale locale) {
        // @formatter:off
        return new Axis()
                .getTitle()
                    .setText(messageSource.getMessage(
                            "graph.axis.title.spending",
                            MessageSource.MessageContext.of(locale)).get())
                    .build()
                .setSoftMax(0)
                .setSoftMin(0)
                .setAllowDecimals(true)
                .setOpposite(false)
                .getLabels()
                    .setFormat("{value}")
                    .build()
                .setType(AxisType.LINEAR);
        // @formatter:om
    }

}
