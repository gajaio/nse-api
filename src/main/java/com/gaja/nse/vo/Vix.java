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
public class Vix {
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
    @JsonProperty("Prev. Close")
    private Double prevClose;
    @JsonProperty("Change")
    private Double change;
    @JsonProperty("% Change")
    private Float percentageChange;
}
