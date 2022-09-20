package com.gaja.nse.vo;

import com.gaja.nse.config.Index;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Scrip {
    private String companyName;
    private String symbol;
    private String isin;
    private String industry;
    private boolean fnosec;
    private String sector;
    private Double pe;
    private Double sectorPe;
    private String derivatives;
    private Double faceValue;
    private BigDecimal cap;
    private Index index;
}
