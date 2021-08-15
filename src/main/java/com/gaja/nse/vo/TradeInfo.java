package com.gaja.nse.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TradeInfo {
    private Double totalTradedVolume;
    private Double totalTradedValue;
    private Double totalMarketCap;
    private Double ffmc;
    private Double impactCost;
    private Double deliveryQuantity;
    private Double deliveryToTradedPercent;
    private boolean areBlockDeals;
}
