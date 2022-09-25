package com.gaja.nse.utils;

public interface NseConstants {
    //urls
    final String API_EQUITY_STOCK_INDICES = "/api/equity-stockIndices?index={index}";
    final String API_SCRIP_DATA = "/api/quote-equity?symbol={scripName}";
    final String API_SCRIP_DATA_TRADE_INFO = "/api/quote-equity?symbol={scripName}&section=trade_info";
    final String API_BULKDEAL_URL = "/api/historical/cm/bulkAndblock?symbol={scripName}";
    final String API_OPTION_CHAIN_EQUITIES = "/api/option-chain-equities?symbol={scripName}";

    //file path
    final String PATH_SCRIP_RESPONSE_TRANSFORM_JSON = "classpath:scripResponseTransform.json";
    final String PATH_COMPANY_DATA_JSON = "classpath:scripFromInfo.json";

    //general
    final String MOZILLA_CLIENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:76.0) Gecko/20100101 Firefox/76.0";
}
