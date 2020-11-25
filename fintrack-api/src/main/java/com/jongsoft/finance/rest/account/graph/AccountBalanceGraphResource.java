package com.jongsoft.finance.rest.account.graph;

import com.jongsoft.finance.core.date.DateRangeOld;
import com.jongsoft.finance.core.date.DateUtils;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.filter.RequestAttributes;
import com.jongsoft.finance.rest.DateFormat;
import com.jongsoft.highchart.Highchart;
import com.jongsoft.highchart.axis.Axis;
import com.jongsoft.highchart.axis.AxisType;
import com.jongsoft.highchart.common.GraphColor;
import com.jongsoft.highchart.common.SeriesType;
import com.jongsoft.highchart.series.LineSeries;
import com.jongsoft.highchart.series.SeriesFactory;
import com.jongsoft.highchart.series.SeriesPoint;
import com.jongsoft.lang.Collections;
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
import org.apache.commons.lang3.mutable.MutableDouble;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

@Tag(name = "Graph Generation")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/accounts/{id}/transactions/graph/balance/{start}/{end}")
public class AccountBalanceGraphResource {

    private final MessageSource messageSource;

    private final FilterFactory filterFactory;
    private final AccountProvider accountProvider;
    private final TransactionProvider transactionProvider;
    private final CurrencyProvider currencyProvider;

    public AccountBalanceGraphResource(
            MessageSource messageSource,
            FilterFactory filterFactory,
            AccountProvider accountProvider,
            TransactionProvider transactionProvider,
            CurrencyProvider currencyProvider) {
        this.messageSource = messageSource;
        this.filterFactory = filterFactory;
        this.accountProvider = accountProvider;
        this.transactionProvider = transactionProvider;
        this.currencyProvider = currencyProvider;
    }

    @Get
    @Operation(
            summary = "Account Balance Graph",
            description = "Generate an account balance graph",
            parameters = {
                    @Parameter(name = "id", in = ParameterIn.PATH, description = "The account identifier", schema = @Schema(implementation = Long.class)),
                    @Parameter(name = "start", in = ParameterIn.PATH, description = "The start date", schema = @Schema(implementation = LocalDate.class)),
                    @Parameter(name = "end", in = ParameterIn.PATH, description = "The end date", schema = @Schema(implementation = LocalDate.class))
            }
    )
    String balance(
            @PathVariable long id,
            @PathVariable @DateFormat LocalDate start,
            @PathVariable @DateFormat LocalDate end,
            @RequestAttribute(RequestAttributes.LOCALIZATION) Locale locale) {
        var account = accountProvider.lookup(id).get();

        final Highchart chart = new Highchart();
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
                .setPointFormat(currencySymbol(account.getCurrency()) + "{point.y}")
                .build()
            .addSeries(createSeries(account, start, end, locale))
            .addYAxis(createBalanceAxis(locale, account))
            .addXAxis(createDateAxis(start, end, locale))
            .getPlotOptions()
                .getSeries()
                    .getMarker()
                        .setEnabled(false)
                        .build();
        // @formatter:on
        return chart.toJson();
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

    private Axis createBalanceAxis(Locale locale, Account account) {
        // @formatter:off
        return new Axis()
                .getTitle()
                    .setText(messageSource.getMessage("graph.axis.title.balance", MessageSource.MessageContext.of(locale)).get())
                    .build()
                .setSoftMax(0)
                .setSoftMin(0)
                .setAllowDecimals(true)
                .getLabels()
                    .setFormat(currencySymbol(account.getCurrency()) + " {value}")
                    .build()
                .setType(AxisType.LINEAR);
        // @formatter:om
    }

    private LineSeries createSeries(Account account, LocalDate start, LocalDate end, Locale locale) {
        LineSeries series = SeriesFactory.createSeries(SeriesType.LINE);

        series.setName(messageSource.getMessage("graph.series.balance", MessageSource.MessageContext.of(locale)).get())
                .setColor(new GraphColor("green"))
                .setNegativeColor(new GraphColor("red"));

        var startBalance = transactionProvider.balance(filterFactory.transaction()
                .accounts(Collections.List(new EntityRef(account.getId())))
                .range(DateRangeOld.of(LocalDate.of(1900, 1, 1), start.minusDays(1))));
        var endBalance = transactionProvider.balance(filterFactory.transaction()
                .accounts(Collections.List(new EntityRef(account.getId())))
                .range(DateRangeOld.of(LocalDate.of(1900, 1, 1), end)));

        var filter = filterFactory.transaction()
                .accounts(Collections.List(new EntityRef(account.getId())))
                .range(DateRangeOld.of(start, end));

        MutableDouble mutableDouble = new MutableDouble(startBalance.getOrSupply(() -> 0D));

        var transactionMap = new HashMap<LocalDate, Double>();
        transactionProvider.daily(filter)
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
        series.setName(account.getName());

        return series;
    }

    private String currencySymbol(String code) {
        return currencyProvider.lookup(code)
                .map(Currency::getSymbol)
                .map(String::valueOf)
                .blockingGet("");
    }
}
