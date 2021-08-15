package com.gaja.nse.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class OHLC {
    @JsonProperty("DATE1")
    private String date;
    @JsonProperty("SYMBOL")
    private String symbol;
    @JsonProperty("SERIES")
    private String series;
    @JsonProperty("PREV_CLOSE")
    private String prevClose;
    @JsonProperty("HIGH_PRICE")
    private float high;
    @JsonProperty("LOW_PRICE")
    private float low;
    @JsonProperty("OPEN_PRICE")
    private float open;
    @JsonProperty("CLOSE_PRICE")
    private float close;
    @JsonProperty("LAST_PRICE")
    private float ltp;
    @JsonProperty("TTL_TRD_QNTY")
    private long totalVol;
    @JsonProperty("TURNOVER_LACS")
    private double totalValue;
    @JsonProperty("NO_OF_TRADES")
    private long noOfTrades;
    @JsonProperty("DELIV_QTY")
    private String deliveryQty;
    @JsonProperty("DELIV_PER")
    private String deliveryPercentage;
    @JsonProperty("AVG_PRICE")
    private float vwap;

}
