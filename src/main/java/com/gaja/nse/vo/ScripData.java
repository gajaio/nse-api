package com.gaja.nse.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScripData {
    private String scripName;
    private String companyName;
    private String industry;
    private String sectorIndex;
    private boolean fnosec;
    private String lastUpdateTime;
    private float change;
    private Double ltp;
    private float percentageChange;
    private float yearHigh;
    private float yearLow;
    private float open;
    private float high;
    private float low;
    private float prevDayClose;
    private Double totalTradedVolume;
    private BigDecimal totalTradedValue;
    private TradeInfo tradeData;
}