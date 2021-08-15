package com.gaja.nse.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ScripDealInfo implements Comparable<ScripDealInfo>{
    private String scripName;
    private String companyName;
    private String industry;
    private Double ltp;
    private String sectorIndex;
    private OptionChain currentMonth;
    private Double deliveryPercentage;
    private List<BulkDeal> bulkDeals;
    private String bulkDealLine;

    @Override
    public int compareTo(ScripDealInfo scripDealInfo) {
        return deliveryPercentage.compareTo(scripDealInfo.getDeliveryPercentage())*-1;
    }
}