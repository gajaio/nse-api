package com.gaja.nse.config;

public enum Index {
    NIFTY_50("NIFTY 50"),
    NIFTY_NEXT_50("NIFTY NEXT 50"),
    NIFTY_100("NIFTY 100"),
    NIFTY_200("NIFTY 200"),
    NIFTY_500("NIFTY 500"),
    NIFTY_MIDCAP_50("NIFTY MIDCAP 50"),
    NIFTY_MIDCAP_100("NIFTY MIDCAP 100"),
    NIFTY_SMLCAP_100("NIFTY SMLCAP 100"),
    INDIA_VIX("INDIA VIX"),
    NIFTY_MIDCAP_150("NIFTY MIDCAP 150"),
    NIFTY_SMLCAP_50("NIFTY SMLCAP 50"),
    NIFTY_SMLCAP_250("NIFTY SMLCAP 250"),
    NIFTY_MIDSML_400("NIFTY MIDSML 400"),
    NIFTY_BANK("NIFTY BANK"),
    NIFTY_AUTO("NIFTY AUTO"),
    NIFTY_FIN_SERVICE("NIFTY FIN SERVICE"),
    NIFTY_FINSRV25_50("NIFTY FINSRV25 50"),
    NIFTY_FMCG("NIFTY FMCG"),
    NIFTY_IT("NIFTY IT"),
    NIFTY_MEDIA("NIFTY MEDIA"),
    NIFTY_METAL("NIFTY METAL"),
    NIFTY_PHARMA("NIFTY PHARMA"),
    NIFTY_PSU_BANK("NIFTY PSU BANK"),
    NIFTY_PVT_BANK("NIFTY PVT BANK"),
    NIFTY_REALTY("NIFTY REALTY"),
    NIFTY_DIV_OPPS_50("NIFTY DIV OPPS 50"),
    NIFTY_GROWSECT_15("NIFTY GROWSECT 15"),
    NIFTY100_QUALTY30("NIFTY100 QUALTY30"),
    NIFTY50_VALUE_20("NIFTY50 VALUE 20"),
    NIFTY50_TR_2X_LEV("NIFTY50 TR 2X LEV"),
    NIFTY50_PR_2X_LEV("NIFTY50 PR 2X LEV"),
    NIFTY50_TR_1X_INV("NIFTY50 TR 1X INV"),
    NIFTY50_PR_1X_INV("NIFTY50 PR 1X INV"),
    NIFTY50_DIV_POINT("NIFTY50 DIV POINT"),
    NIFTY_ALPHA_50("NIFTY ALPHA 50"),
    NIFTY50_EQL_WGT("NIFTY50 EQL WGT"),
    NIFTY100_EQL_WGT("NIFTY100 EQL WGT"),
    NIFTY100_LOWVOL30("NIFTY100 LOWVOL30"),
    NIFTY200_QUALTY30("NIFTY200 QUALTY30"),
    NIFTY_ALPHALOWVOL("NIFTY ALPHALOWVOL"),
    NIFTY200MOMENTM30("NIFTY200MOMENTM30"),
    NIFTY_COMMODITIES("NIFTY COMMODITIES"),
    NIFTY_CONSUMPTION("NIFTY CONSUMPTION"),
    NIFTY_CPSE("NIFTY CPSE"),
    NIFTY_ENERGY("NIFTY ENERGY"),
    NIFTY_INFRA("NIFTY INFRA"),
    NIFTY100_LIQ_15("NIFTY100 LIQ 15"),
    NIFTY_MID_LIQ_15("NIFTY MID LIQ 15"),
    NIFTY_MNC("NIFTY MNC"),
    NIFTY_PSE("NIFTY PSE"),
    NIFTY_SERV_SECTOR("NIFTY SERV SECTOR"),
    NIFTY100ESGSECLDR("NIFTY100ESGSECLDR"),
    NIFTY_GS_8_13YR("NIFTY GS 8 13YR"),
    NIFTY_GS_10YR("NIFTY GS 10YR"),
    NIFTY_GS_10YR_CLN("NIFTY GS 10YR CLN"),
    NIFTY_GS_4_8YR("NIFTY GS 4 8YR"),
    NIFTY_GS_11_15YR("NIFTY GS 11 15YR"),
    NIFTY_GS_15YRPLUS("NIFTY GS 15YRPLUS"),
    NIFTY_GS_COMPSITE("NIFTY GS COMPSITE")
    ;
    private String indexValue;

    Index(String indexValue) {
        this.indexValue = indexValue;
    }

    public String getIndexValue() {
        return indexValue;
    }
}
