package com.gaja.nse.utils;

import com.gaja.nse.vo.OHLCArchieve;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.candles.BullishEngulfingIndicator;
import org.ta4j.core.indicators.candles.DojiIndicator;
import org.ta4j.core.num.PrecisionNum;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CandleStickUtl {
    public static boolean isHammer(BarSeries series, int index){
        DojiIndicator dojiIndicator = new DojiIndicator(series, 14, .2);
        return dojiIndicator.getValue(index);
    }
    public static boolean isMorningStar(Bar bar1, Bar bar2, Bar bar3, Bar bar4) {
        return //bar4.getClosePrice().minus(bar4.getOpenPrice()).dividedBy(bar4.getClosePrice()).isGreaterThan(PrecisionNum.valueOf(0.03)) &&
                 bar3.getClosePrice().minus(bar3.getOpenPrice()).dividedBy(bar3.getHighPrice().minus(bar3.getLowPrice())).isLessThan(PrecisionNum.valueOf(0.02))
                && bar4.getClosePrice().isGreaterThan(bar2.getOpenPrice())
                && bar1.getOpenPrice().isGreaterThan(bar4.getClosePrice());
    }

    public static boolean isBullishEngulf(BarSeries series) {
        BullishEngulfingIndicator bullishEngulfingIndicator = new BullishEngulfingIndicator(series);
        return IntStream.range(series.getBarCount() - 6, series.getBarCount()).filter(i -> bullishEngulfingIndicator.getValue(i).booleanValue())
                .findAny()
                .isPresent();
    }

    public static boolean isDownTrendIgniteCandle(OHLCArchieve ohlcDay) {
        return ohlcDay.getLow() == ohlcDay.getClose()
                && ohlcDay.getHigh() == ohlcDay.getOpen()
                && ((ohlcDay.getOpen() - ohlcDay.getClose()) % ohlcDay.getClose()) * 100 > 4.5;
    }

    public static Optional<Bar> periodMin(BarSeries ohlcCandles, int n, Function<Bar, Float> compreKey) {
        return IntStream.range(ohlcCandles.getBarCount() - n, ohlcCandles.getBarCount()).mapToObj(i -> ohlcCandles.getBar(i)).reduce((t1, t2) -> compreKey.apply(t1) < compreKey.apply(t2) ? t1 : t2);
    }

    public static Optional<Bar> periodMax(BarSeries ohlcCandles, int n, Function<Bar, Float> compreKey) {
        return IntStream.range(ohlcCandles.getBarCount() - n, ohlcCandles.getBarCount()).mapToObj(i -> ohlcCandles.getBar(i)).reduce((t1, t2) -> compreKey.apply(t1) > compreKey.apply(t2) ? t1 : t2);
    }

    public static Optional<OHLCArchieve> periodMin(List<OHLCArchieve> ohlcCandles, int n, Function<OHLCArchieve, Float> compreKey) {
        return ohlcCandles.stream().reduce((t1, t2) -> compreKey.apply(t1) < compreKey.apply(t2) ? t1 : t2);
    }

    public static Optional<OHLCArchieve> periodMax(List<OHLCArchieve> ohlcCandles, int n, Function<OHLCArchieve, Float> compreKey) {
        return ohlcCandles.stream().reduce((t1, t2) -> compreKey.apply(t1) > compreKey.apply(t2) ? t1 : t2);
    }

    public static BarSeries getBarSeries(List<OHLCArchieve> ohlc, String scrip) {
        BarSeries series = new BaseBarSeriesBuilder().withName(scrip).build();
        ohlc.forEach(ohlcArchieve -> series.addBar(
                ZonedDateTime.from(LocalDate.parse(ohlcArchieve.getDate(), DateTimeFormatter.ofPattern("dd-MMM-yyyy")).atStartOfDay(ZoneId.systemDefault())),
                ohlcArchieve.getOpen(),
                ohlcArchieve.getHigh(),
                ohlcArchieve.getLow(),
                ohlcArchieve.getClose(),
                ohlcArchieve.getTotalVol()));
        return series;
    }

    public static BarSeries getBarSeriesMonthly(List<OHLCArchieve> ohlc, String scrip) {
        BarSeries series = new BaseBarSeriesBuilder().withName(scrip).build();
        List<List<OHLCArchieve>> monthly = sortData(getMonthlyData(ohlc));
        monthly.stream().forEach(ohlcArchieves -> {
            addBarData(series, ohlcArchieves);
        });

        return series;
    }

    public static BarSeries getBarSeriesWeekly(List<OHLCArchieve> ohlc, String scrip) {
        BarSeries series = new BaseBarSeriesBuilder().withName(scrip).build();
        List<List<OHLCArchieve>> monthly = getMonthlyData(ohlc);

        List<List<OHLCArchieve>> weekly = monthly.stream().flatMap(ohlcArchieves -> ohlcArchieves.stream()
                .collect(Collectors.groupingBy(ohlcArchieve -> {
                    LocalDate day = LocalDate.parse(ohlcArchieve.getDate(), DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
                    return day.get(WeekFields.of(Locale.getDefault()).weekOfYear());
                })).values().stream()).collect(Collectors.toList());
        List<List<OHLCArchieve>> sortedWeekly = sortData(weekly);
        sortedWeekly.stream().forEach(ohlcArchieves -> {
            addBarData(series, ohlcArchieves);
        });

        return series;
    }

    private static List<List<OHLCArchieve>> sortData(List<List<OHLCArchieve>> weekly) {
        return weekly.stream()
                .map(ohlcArchieves -> ohlcArchieves.stream().sorted(Comparator.comparing(ohlcArchieve -> LocalDate.parse(ohlcArchieve.getDate(), DateTimeFormatter.ofPattern("dd-MMM-yyyy")))).collect(Collectors.toList()))
                .sorted(Comparator.comparing(ohlcArchieves -> LocalDate.parse(ohlcArchieves.get(0).getDate(), DateTimeFormatter.ofPattern("dd-MMM-yyyy"))))
                .collect(Collectors.toList());
    }

    private static void addBarData(BarSeries series, List<OHLCArchieve> ohlcArchieves) {
        series.addBar(
                ZonedDateTime.from(LocalDate.parse(ohlcArchieves.get(ohlcArchieves.size() - 1).getDate(), DateTimeFormatter.ofPattern("dd-MMM-yyyy")).atStartOfDay(ZoneId.systemDefault())),
                ohlcArchieves.get(0).getOpen(),
                ohlcArchieves.stream().max(Comparator.comparing(OHLCArchieve::getHigh)).get().getHigh(),
                ohlcArchieves.stream().min(Comparator.comparing(OHLCArchieve::getLow)).get().getLow(),
                ohlcArchieves.get(ohlcArchieves.size() - 1).getClose(),
                ohlcArchieves.stream().map(ohlcArchieve -> ohlcArchieve.getTotalVol()).reduce((o1, o2) -> o1 + o2).get());
    }

    private static List<List<OHLCArchieve>> getMonthlyData(List<OHLCArchieve> ohlc) {
        Map<Integer, List<OHLCArchieve>> groupedByYear = ohlc.stream()
                .collect(Collectors.groupingBy(
                        ohlcArchieve -> {
                            LocalDate day = LocalDate.parse(ohlcArchieve.getDate(), DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
                            return day.getYear();
                        }
                ));
        return groupedByYear.values().stream()
                .flatMap(ohlcArchieves -> ohlcArchieves.stream()
                        .collect(Collectors.groupingBy(ohlcArchieve -> {
                            LocalDate day = LocalDate.parse(ohlcArchieve.getDate(), DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
                            return day.getMonthValue();
                        })).values().stream()).collect(Collectors.toList());
    }

}
