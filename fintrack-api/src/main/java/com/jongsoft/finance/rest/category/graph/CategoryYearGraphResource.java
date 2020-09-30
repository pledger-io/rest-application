package com.jongsoft.finance.rest.category.graph;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.core.date.Dates;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.filter.RequestAttributes;
import com.jongsoft.highchart.Highchart;
import com.jongsoft.highchart.axis.Axis;
import com.jongsoft.highchart.axis.AxisType;
import com.jongsoft.highchart.common.GraphColor;
import com.jongsoft.highchart.common.SeriesType;
import com.jongsoft.highchart.series.BarSeries;
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
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.Locale;

@Tag(name = "Graph Generation")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/categories/{year}/graph")
public class CategoryYearGraphResource {

    private final MessageSource messageSource;
    private final FilterFactory filterFactory;
    private final TransactionProvider transactionService;
    private final CategoryProvider categoryService;

    public CategoryYearGraphResource(
            MessageSource messageSource,
            FilterFactory filterFactory,
            TransactionProvider transactionService,
            CategoryProvider categoryService) {
        this.messageSource = messageSource;
        this.filterFactory = filterFactory;
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    @Get
    @Operation(
            summary = "Category Year",
            description = "Generate a category yearly income vs expenses graph",
            operationId = "generateCategoryYearGraph"
    )
    Highchart chart(
            @PathVariable int year,
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale,
            @RequestAttribute(RequestAttributes.CURRENCY) Currency currency) {
        if (currency == null) {
            currency = Currency.builder().build();
        }
        var incomeSeries = SeriesFactory.<BarSeries>createSeries(SeriesType.COLUMN);
        var expenseSeries = SeriesFactory.<BarSeries>createSeries(SeriesType.COLUMN);

        var currentStart = LocalDate.of(year, 1, 1);
        var totalEnd = LocalDate.of(year, 12, 31);

        var builder = filterFactory.transaction()
                .ownAccounts()
                .currency(currency.getCode())
                .categories(categoryService.lookup()
                        .map(c -> new EntityRef(c.getId())));

        expenseSeries.setName(messageSource.getMessage("graph.series.expenses",
                MessageSource.MessageContext.of(locale)).get())
                .setPointWidth(50)
                .setColor(new GraphColor("#dc3545"));
        incomeSeries.setName(messageSource.getMessage("graph.series.income",
                MessageSource.MessageContext.of(locale)).get())
                .setPointWidth(50)
                .setColor(new GraphColor("#7fc6a5"));

        while (currentStart.isBefore(totalEnd)) {
            builder.range(DateRange.forMonth(currentStart.getYear(), currentStart.getMonthValue()));

            var income = transactionService.balance(builder.onlyIncome(true));
            var expense = transactionService.balance(builder.onlyIncome(false));

            var timestamp = Dates.timestamp(currentStart);
            incomeSeries.addPoint(new SeriesPoint().setX(timestamp).setY(income.getOrSupply(() -> 0D)));
            expenseSeries.addPoint(new SeriesPoint().setX(timestamp).setY(expense.getOrSupply(() -> 0D)));

            currentStart = currentStart.plusMonths(1);
        }

        return new Highchart()
                .addXAxis(createDateAxis(
                        LocalDate.of(year, 1, 1),
                        LocalDate.of(year, 12, 31)))
                .addYAxis(new Axis()
                        .setType(AxisType.LINEAR)
                        .getTitle()
                            .setText("")
                            .build()
                        .setSoftMax(0)
                        .setSoftMin(0)
                        .getLabels()
                            .setFormat(currency.getSymbol() + "{value}")
                        .build())
                .addSeries(incomeSeries)
                .addSeries(expenseSeries)
                .getTitle()
                    .setText("")
                    .build()
                .getChart()
                    .setType(null)
                    .build()
                .getLegend()
                    .setEnabled(false)
                    .build()
                .getTooltip()
                    .setShared(false)
                    .setValueDecimals(2)
                    .setHeaderFormat("<strong>{series.name}</strong><br />")
                    .setPointFormat(currency.getSymbol() + "{point.y}")
                    .build()
                .getCredits()
                    .setText("FinTrack")
                    .setEnabled(false)
                    .build();
    }

    private Axis createDateAxis(LocalDate start, LocalDate end) {
        // @formatter:off
        return new Axis()
                .getTitle()
                    .setText("")
                    .build()
                .setMin(Dates.timestamp(start))
                .setMax(Dates.timestamp(end))
                .setMinTickInterval(3600000 * 24)
                .setType(AxisType.DATETIME);
        // @formatter:om
    }
}
