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
public class OHLCArchieve {
    @JsonProperty("Symbol")
    private String symbol;
    @JsonProperty("Series")
    private String series;
    @JsonProperty("Date")
    private String date;
    @JsonProperty("Prev Close")
    private float prevClose;
    @JsonProperty("Open Price")
    private float open;
    @JsonProperty("High Price")
    private float high;
    @JsonProperty("Low Price")
    private float low;
    @JsonProperty("Last Price")
    private float ltp;
    @JsonProperty("Close Price")
    private float close;
    @JsonProperty("Average Price")
    private float vwap;
    @JsonProperty("Turnover")
    private double totalValue;
    @JsonProperty("No. of Trades")
    private long noOfTrades;
    @JsonProperty("Total Traded Quantity")
    private long totalVol;
    @JsonProperty("Deliverable Qty")
    private double deliveryQty;
    @JsonProperty("% Dly Qt to Traded Qty")
    private float percentageDelivery;
}
