package com.jongsoft.finance.rest.statistic.graph;

import com.jongsoft.finance.core.date.DateRangeOld;
import com.jongsoft.finance.core.date.DateUtils;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.filter.RequestAttributes;
import com.jongsoft.finance.rest.DateFormat;
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
import org.apache.commons.lang3.mutable.MutableDouble;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

@Tag(name = "Reports")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/statistics/balance/graph/{from}/{until}")
public class BalanceGraphResource {

    private final MessageSource messageSource;
    private final CurrentUserProvider currentUserProvider;
    private final TransactionProvider transactionProvider;

    private final FilterFactory filterFactory;

    public BalanceGraphResource(
            MessageSource messageSource,
            CurrentUserProvider currentUserProvider,
            TransactionProvider transactionProvider,
            FilterFactory filterFactory) {
        this.messageSource = messageSource;
        this.currentUserProvider = currentUserProvider;
        this.transactionProvider = transactionProvider;
        this.filterFactory = filterFactory;
    }

    @Get
    @Operation(
            operationId = "reportBalanceGraph",
            summary = "Generate a graph JSON for balance of all accounts",
            description = "This operation will generate an balance graph for the provided date range",
            parameters = {
                    @Parameter(name = "from", in = ParameterIn.PATH, schema = @Schema(implementation = String.class)),
                    @Parameter(name = "until", in = ParameterIn.PATH, schema = @Schema(implementation = String.class)),
                    @Parameter(name = HttpHeaders.ACCEPT_LANGUAGE, in = ParameterIn.HEADER, required = true, example = "en")
            }
    )
    String balance(
            @PathVariable @DateFormat LocalDate from,
            @PathVariable @DateFormat LocalDate until,
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale) {
        var chart = new Highchart();
        // @formatter:off
        chart.getTitle()
                .setText("")
                .build()
            .getChart()
                .setType(SeriesType.LINE)
                .setHeight(350)
                .build()
            .getCredits()
                .setEnabled(false)
                .setText("Personal finance")
                .build()
            .getLegend()
                .setEnabled(false)
                .build()
            .getTooltip()
                .setValueDecimals(2)
                .setShared(false)
                .setPointFormat(getCurrency(locale) + "{point.y}")
                .build()
            .addSeries(createSeries(from, until, locale))
            .addYAxis(createBalanceAxis(locale))
            .addXAxis(createDateAxis(from, until, locale))
            .getPlotOptions()
            .getSeries()
                .getMarker()
                .setEnabled(false)
                .build();
        // @formatter:on
        return chart.toJson();
    }

    private LineSeries createSeries(LocalDate start, LocalDate end, Locale locale) {
        var series = SeriesFactory.<LineSeries>createSeries(SeriesType.LINE);

        series.setName(messageSource.getMessage("graph.series.balance", MessageSource.MessageContext.of(locale)).get())
                .setColor(new GraphColor("green"))
                .setNegativeColor(new GraphColor("red"));

        var startBalance = transactionProvider.balance(
                filterFactory.transaction()
                        .ownAccounts()
                        .range(DateRangeOld.of(LocalDate.of(1900, 1, 1), start)));
        var endBalance = transactionProvider.balance(
                filterFactory.transaction()
                .ownAccounts()
                .range(DateRangeOld.of(LocalDate.of(1900, 1, 1), end)));

        var dataRequest = filterFactory.transaction()
                .ownAccounts()
                .range(DateRangeOld.of(start, end));

        MutableDouble mutableDouble = new MutableDouble(startBalance.getOrSupply(() -> 0D));

        var transactionMap = new HashMap<LocalDate, Double>();
        transactionProvider.daily(dataRequest)
                .forEach(pair -> transactionMap.put(pair.day(), pair.summary()));

        final int days = (int) ChronoUnit.DAYS.between(start, end);
        Map<LocalDate, Double> monthBalance = new HashMap<>(days);
        IntStream.range(0, days).forEach(number -> {
            var transactionDate = start.plusDays(number);
            if (transactionMap.containsKey(transactionDate)) {
                mutableDouble.add(transactionMap.get(transactionDate));
            }

            monthBalance.put(transactionDate, mutableDouble.doubleValue());
        });
        monthBalance.put(end, endBalance.getOrSupply(() -> 0D));

        monthBalance.entrySet().stream()
                .map(entry -> new SeriesPoint()
                        .setX(DateUtils.timestamp(entry.getKey()))
                        .setY(entry.getValue()))
                .forEach(series::addPoint);

        return series;
    }

    private Axis createDateAxis(LocalDate start, LocalDate end, Locale locale) {
        // @formatter:off
        return new Axis()
                .getTitle()
                    .setText(messageSource.getMessage("graph.axis.title.date", MessageSource.MessageContext.of(locale)).get())
                    .build()
                .setMin(start.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setMax(end.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
                .setMinTickInterval(3600000 * 24)
                .setType(AxisType.DATETIME);
        // @formatter:om
    }

    private Axis createBalanceAxis(Locale locale) {
        // @formatter:off
        return new Axis()
                .getTitle()
                    .setText(messageSource.getMessage("graph.axis.title.balance", MessageSource.MessageContext.of(locale)).get())
                    .build()
                .setSoftMax(0)
                .setSoftMin(0)
                .setAllowDecimals(true)
                .setOpposite(false)
                .getLabels()
                    .setFormat(getCurrency(locale) + " {value}")
                    .build()
                .setType(AxisType.LINEAR);
        // @formatter:om
    }

    private String getCurrency(Locale locale) {
        var currency = currentUserProvider.currentUser().getPrimaryCurrency();
        if (currency == null) {
            currency = Currency.getInstance("EUR");
        }

        return currency.getSymbol(locale);
    }
}
