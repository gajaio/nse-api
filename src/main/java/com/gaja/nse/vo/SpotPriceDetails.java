package com.gaja.nse.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotPriceDetails {
    private long strikePrice;
    private String expiryDate;
    private String underlying;
    private String identifier;
    private long openInterest;
    private long changeinOpenInterest;
    private double pchangeinOpenInterest;
    private BigInteger totalTradedVolume;
    private double impliedVolatility;
    private double lastPrice;
    private double change;
    private double pChange;
    private BigInteger totalBuyQuantity;
    private BigInteger totalSellQuantity;
    private BigInteger bidQty;
    private double bidprice;
    private BigInteger askQty;
    private double askPrice;
    private double underlyingValue;
}
