package com.gaja.nse.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportDaily {
    private int reportType;
    private int slNum;
    private String name;
    private String type;
    private long tradedQuantity;
    private long deliverableQuantity;
    private double percentage;
}
