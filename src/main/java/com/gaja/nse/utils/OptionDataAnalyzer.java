package com.gaja.nse.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaja.nse.config.jayway.JsonPathConfig;
import com.gaja.nse.vo.OptionChain;
import com.gaja.nse.vo.SpotPriceDetails;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class OptionDataAnalyzer {
    private final ObjectMapper mapper;
    private final Configuration jaywayConfig;

    @Autowired
    public OptionDataAnalyzer(ObjectMapper mapper, Configuration jaywayConfig) {
        this.mapper = mapper;
        this.jaywayConfig = jaywayConfig;
        Configuration.setDefaults(new JsonPathConfig());
    }

    public static List<SpotPriceDetails> getSpotPriceDetails(DocumentContext ctx, String expiry, String jsonPath) {
        return ctx.read(jsonPath, new TypeRef<List<SpotPriceDetails>>() {
        })
                .stream()
                .filter(sp -> sp != null)
                .filter(spotPriceDetails -> spotPriceDetails.getExpiryDate().equals(expiry))
                .collect(Collectors.toList());
    }

    public List<OptionChain> getOptionChart(String optionsJson) {
        DocumentContext ctx = JsonPath.using(jaywayConfig).parse(optionsJson);
        List<String> expiryDates = ctx.read("$.records.expiryDates", new TypeRef<List<String>>() {
        });

        Map<String, List<SpotPriceDetails>> callMonthly = expiryDates.stream()
                .flatMap(date -> getCallOptionData(ctx, date))
                .collect(Collectors.groupingBy(o -> o.getExpiryDate()));
        Map<String, List<SpotPriceDetails>> putMonthly = expiryDates.stream()
                .flatMap(date -> getPutOptionData(ctx, date))
                .collect(Collectors.groupingBy(o -> o.getExpiryDate()));
        return expiryDates.stream()
                .map(s -> OptionChain.builder().callData(callMonthly.get(s)).putData(putMonthly.get(s)).build())
                .collect(Collectors.toList());
    }

    public static Stream<SpotPriceDetails> getCallOptionData(DocumentContext ctx, String month) {

        List<SpotPriceDetails> CallOptionsData = getSpotPriceDetails(ctx, month, "$.records.data[*].CE");

        return CallOptionsData.stream().filter(spotPriceDetails -> spotPriceDetails.getExpiryDate().contains(month)).filter(spotPriceDetails -> spotPriceDetails.getStrikePrice() > spotPriceDetails.getUnderlyingValue());
    }

    public static Stream<SpotPriceDetails> getPutOptionData(DocumentContext ctx, String month) {
        List<SpotPriceDetails> putOptionsData = getSpotPriceDetails(ctx, month, "$.records.data[*].PE");
        return putOptionsData.stream().filter(spotPriceDetails -> spotPriceDetails.getExpiryDate().contains(month)).filter(spotPriceDetails -> spotPriceDetails.getStrikePrice() < spotPriceDetails.getUnderlyingValue());
    }
}
