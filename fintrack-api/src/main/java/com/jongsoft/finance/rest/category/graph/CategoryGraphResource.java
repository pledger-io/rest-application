package com.jongsoft.finance.rest.category.graph;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.filter.RequestAttributes;
import com.jongsoft.finance.rest.DateFormat;
import com.jongsoft.highchart.Highchart;
import com.jongsoft.highchart.axis.Axis;
import com.jongsoft.highchart.axis.AxisType;
import com.jongsoft.highchart.common.SeriesType;
import com.jongsoft.highchart.series.ColumnSeries;
import com.jongsoft.highchart.series.SeriesFactory;
import com.jongsoft.highchart.series.SeriesPoint;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;
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
@Controller("/api/categories/graph/{from}/{until}")
public class CategoryGraphResource {

    private final MessageSource messageSource;
    private final FilterFactory filterFactory;
    private final CategoryProvider categoryProvider;
    private final TransactionProvider transactionProvider;

    public CategoryGraphResource(
            MessageSource messageSource,
            FilterFactory filterFactory,
            CategoryProvider categoryProvider,
            TransactionProvider transactionProvider) {
        this.messageSource = messageSource;
        this.filterFactory = filterFactory;
        this.categoryProvider = categoryProvider;
        this.transactionProvider = transactionProvider;
    }

    @Get
    @Operation(
            summary = "Category month graph",
            description = "Generates a bar graph for category expenses",
            operationId = "generateCategoryExpenses"
    )
    String graph(
            @PathVariable @DateFormat LocalDate from,
            @PathVariable @DateFormat LocalDate until,
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale) {
        var xAxis = new Axis();
        var series = (ColumnSeries) SeriesFactory.createSeries(SeriesType.COLUMN);

        series.setName(messageSource.getMessage("graph.series.category",
                MessageSource.MessageContext.of(locale)).get());

        for (Category category : categoryProvider.lookup()) {
            var amount = transactionProvider.balance(filterFactory.transaction()
                    .onlyIncome(false)
                    .ownAccounts()
                    .range(Dates.range(from, until))
                    .categories(Collections.List(new EntityRef(category.getId()))));

            series.addPoint(new SeriesPoint()
                    .setName(category.getLabel())
                    .setY(Math.abs(amount.getOrSupply(() -> 0D))));

            xAxis.addCategory(category.getLabel());
        }

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
                .getTooltip()
                    .setHeaderFormat("")
                    .setPointFormat("<b>{point.name}<b>: {point.y}")
                    .build()
                .addSeries(series)
                .addYAxis(createBalanceAxis(locale))
                .addXAxis(xAxis)
                .toJson();
        // @formatter:on
    }

    private Axis createBalanceAxis(Locale locale) {
        // @formatter:off
        return new Axis()
                .getTitle()
                    .setText(messageSource.getMessage("graph.axis.title.spending",
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
