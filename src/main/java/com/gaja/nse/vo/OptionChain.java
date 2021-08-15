package com.gaja.nse.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class OptionChain {
    @JsonProperty("PE")
    private List<SpotPriceDetails> putData;
    @JsonProperty("CE")
    private List<SpotPriceDetails> callData;
    private String expiryDate;
    private String strikePrice;
}