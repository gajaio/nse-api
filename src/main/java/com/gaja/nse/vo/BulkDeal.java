package com.gaja.nse.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkDeal {
    @JsonProperty("BD_QTY_TRD")
    private BigDecimal tradedQuantity;
    @JsonProperty("BD_TP_WATP")
    private Float price;
    @JsonProperty("BD_DT_DATE")
    private String date;
    @JsonProperty("BD_CLIENT_NAME")
    private String client;
    @JsonProperty("BD_BUY_SELL")
    private String type;
}
