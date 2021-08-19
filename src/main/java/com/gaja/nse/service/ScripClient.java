package com.gaja.nse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gaja.nse.config.Index;
import com.gaja.nse.vo.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public interface ScripClient {

    List<ScripData> getAll(Index index) throws IOException;

    void processAllScrips(List<String> excluded, int batchSize, Consumer<List<Scrip>> onEachBatchComplete) throws IOException;

    OHLC getTradeData(String scrip) throws IOException;

    List<OHLCArchieve> getPastOHLCData(String scrip) throws IOException;

    List<OHLCArchieve> getPastOHLCData(String scrip, LocalDate start) throws IOException;

    List<BulkDeal> getDeals(String scripName) throws IOException;

    List<OptionChain> getOptionChain(String scripName) throws JsonProcessingException;
}
