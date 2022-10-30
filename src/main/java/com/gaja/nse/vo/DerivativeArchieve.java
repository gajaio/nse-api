package com.gaja.nse.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DerivativeArchieve {
    @JsonProperty("Symbol")
    private String symbol;
    @JsonProperty("Date")
    private String date;
    @JsonProperty("Expiry")
    private String expiry;
    @JsonProperty("Option Type")
    private String optionType;
    @JsonProperty("Open")
    private Double open;
    @JsonProperty("High")
    private Double high;
    @JsonProperty("Low")
    private Double low;
    @JsonProperty("LTP")
    private Double ltp;
    @JsonProperty("Close")
    private Double close;
    @JsonProperty("Strike Price")
    private Double strikePrice;
    @JsonProperty("Settle Price")
    private Double settlePrice;
    @JsonProperty("Turnover in Lacs")
    private BigDecimal turnOver;
    @JsonProperty("Premium Turnover in Lacs")
    private BigDecimal premiumTurnOver;
    @JsonProperty("Open Int")
    private long openInterest;
    @JsonProperty("No. of contracts")
    private long totalContracts;
    @JsonProperty("Change in OI")
    private Long oiChange;
    @JsonProperty("Underlying Value")
    private BigDecimal underlyingValue;
}