package com.gaja.nse.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonProperty("Open")
    private float open;
    @JsonProperty("High")
    private float high;
    @JsonProperty("Low")
    private float low;
    @JsonProperty("LTP")
    private float ltp;
    @JsonProperty("Close")
    private float close;
    @JsonProperty("Settle Price")
    private double settlePrice;
    @JsonProperty("Turnover in Lacs")
    private double turnOver;
    @JsonProperty("Open Int")
    private long noOfTrades;
    @JsonProperty("No. of contracts")
    private long totalContracts;
    @JsonProperty("% Change in OI")
    private float oiChange;
    @JsonProperty("Underlying Value")
    private double underlyingValue;
}
