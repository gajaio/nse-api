package com.gaja.nse.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceInfo {
    private double ltp;
    private double change;
    private double faceValue;
    private double pe;
    private double sectorPe;
    private double percentageChange;
}
