package com.gaja.nse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gaja.nse.config.Index;
import com.gaja.nse.vo.*;

import java.io.IOException;
import java.util.List;

public interface ScripClient {

    List<ScripData> getAll(Index index) throws IOException;

    List<Scrip> getAll(List<String> excluded) throws IOException;

    OHLC getTradeData(String scrip) throws IOException;

    List<OHLCArchieve> getPastOHLCData(String scrip) throws IOException;

    List<BulkDeal> getDeals(String scripName) throws IOException;

    List<OptionChain> getOptionChain(String scripName) throws JsonProcessingException;
}
