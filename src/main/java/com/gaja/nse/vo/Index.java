package com.gaja.nse.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Index {
    @JsonProperty("Date")
    private String date;
    @JsonProperty("Open")
    private Double open;
    @JsonProperty("High")
    private Double high;
    @JsonProperty("Low")
    private Double low;
    @JsonProperty("Close")
    private Double close;
    @JsonProperty("Shares Traded")
    private Long sharesTraded;
    @JsonProperty("Turnover (Rs. Cr)")
    private Double turnover;
}
