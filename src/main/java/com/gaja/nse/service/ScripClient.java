package com.gaja.nse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gaja.nse.config.Index;
import com.gaja.nse.vo.*;
import lombok.SneakyThrows;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public interface ScripClient {

    List<ScripData> getAll(Index index) throws IOException;

    @SneakyThrows
    void processAllScripsForIndex(List<String> excluded, Index index, int batchSize, Consumer<List<Scrip>> onEachBatchComplete);

    void processAllScrips(List<String> excluded, int batchSize, Consumer<List<Scrip>> onEachBatchComplete) throws IOException;

    OHLC getTradeData(String scrip) throws IOException;


    void getPastOHLCData(String scrip, Consumer<List<OHLCArchieve>> onBatch) throws IOException;

    void getPastOHLCData(String scrip, LocalDate start, Consumer<List<OHLCArchieve>> onBatch) throws IOException;

    void getPastOptionsData(String scrip, boolean isIndex, LocalDate start, String optionType, Consumer<List<DerivativeArchieve>> onBatch) throws IOException;

    void getPastOptionsData(String scrip, boolean isIndex, String optionType, Consumer<List<DerivativeArchieve>> onBatch) throws IOException;

    List<BulkDeal> getDeals(String scripName) throws IOException;

    List<OptionChain> getOptionChain(String scripName) throws JsonProcessingException;

    String getOrderBook(String scrip);
}
