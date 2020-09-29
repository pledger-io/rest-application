package com.jongsoft.finance.graph;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.highchart.Highchart;
import com.jongsoft.highchart.common.GraphColor;
import com.jongsoft.highchart.common.SeriesType;
import com.jongsoft.highchart.series.PieSeries;
import com.jongsoft.highchart.series.SeriesFactory;
import com.jongsoft.highchart.series.SeriesPoint;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

public class CategoryPieChart {

    private final MessageSource messageSource;
    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;
    private final CategoryProvider categoryProvider;

    public CategoryPieChart(
            MessageSource messageSource,
            FilterFactory filterFactory,
            TransactionProvider transactionProvider,
            CategoryProvider categoryProvider) {
        this.messageSource = messageSource;
        this.filterFactory = filterFactory;
        this.transactionProvider = transactionProvider;
        this.categoryProvider = categoryProvider;
    }

    protected Highchart createChart(Currency currency, Locale locale) {
        final Highchart chart = new Highchart();

        // @formatter:off
        chart.getTitle()
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
                .setPointFormat(currency.getSymbol() + "{point.y}")
                .build()
            .getCredits()
                .setEnabled(false)
                .setText("FinTrack")
                .build();
        // @formatter:on
        return chart;
    }

    protected PieSeries createSeries(
            Sequence<Account> accounts,
            LocalDate start,
            LocalDate end,
            Locale locale,
            boolean income,
            Currency currency) {
        PieSeries series = SeriesFactory.createSeries(SeriesType.PIE);

        series.setName(messageSource.getMessage("graph.series.category", MessageSource.MessageContext.of(locale)).get());
        series.getDataLabels().setEnabled(false);

        var filter = filterFactory.transaction()
                .onlyIncome(income)
                .currency(currency.getCode())
                .range(DateRange.of(start, end));
        if (accounts.isEmpty()) {
            filter.ownAccounts();
        } else {
            filter.accounts(accounts.map(account -> new EntityRef(account.getId())));
        }

        var totalBalance = BigDecimal.valueOf(transactionProvider.balance(filter).getOrSupply(() -> 0D));

        for (Category category : categoryProvider.lookup()) {
            double balance = transactionProvider.balance(
                    filter.categories(API.List(new EntityRef(category.getId()))))
                    .getOrSupply(() -> 0D);

            totalBalance = totalBalance.subtract(BigDecimal.valueOf(balance));
            SeriesPoint point = new SeriesPoint()
                    .setName(category.getLabel())
                    .setY(Math.abs(balance));
            series.addPoint(point);
        }

        series.addPoint(new SeriesPoint()
                .setName(messageSource.getMessage("Category.none", MessageSource.MessageContext.of(locale)).get())
                .setY(Math.abs(totalBalance.doubleValue())));

        return series;
    }

}
