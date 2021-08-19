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
    private Float prevClose;
    @JsonProperty("Open Price")
    private Float open;
    @JsonProperty("High Price")
    private Float high;
    @JsonProperty("Low Price")
    private Float low;
    @JsonProperty("Last Price")
    private Float ltp;
    @JsonProperty("Close Price")
    private Float close;
    @JsonProperty("Average Price")
    private Float vwap;
    @JsonProperty("Turnover")
    private Double totalValue;
    @JsonProperty("No. of Trades")
    private Long noOfTrades;
    @JsonProperty("Total Traded Quantity")
    private Long totalVol;
    @JsonProperty("Deliverable Qty")
    private Long deliveryQty;
    @JsonProperty("% Dly Qt to Traded Qty")
    private Float percentageDelivery;
}
