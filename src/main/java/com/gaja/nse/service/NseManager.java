package com.gaja.nse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaja.nse.annotations.NseHome;
import com.gaja.nse.config.Index;
import com.gaja.nse.config.NseServiceProperties;
import com.gaja.nse.transformer.NseBulkDealTransformer;
import com.gaja.nse.transformer.NseScripTransformer;
import com.gaja.nse.utils.NseConstants;
import com.gaja.nse.utils.OptionDataAnalyzer;
import com.gaja.nse.utils.StockUtils;
import com.gaja.nse.vo.*;
import com.jayway.jsonpath.Configuration;
import lombok.SneakyThrows;
import org.apache.commons.collections4.ListUtils;
import org.apache.http.client.HttpResponseException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.gaja.nse.utils.NseConstants.*;
import static com.gaja.nse.utils.StockUtils.bhavCopy;
import static com.gaja.nse.utils.StockUtils.getHeaders;

@Component
@EnableConfigurationProperties(NseServiceProperties.class)
public class NseManager implements ApplicationContextAware {
    private final NseServiceProperties properties;
    private ApplicationContext applicationContext;
    private final NseScripTransformer nseScripTransformer;
    private final NseBulkDealTransformer nseBulkDealTransformer;
    private final RestTemplate nseDataTemplate;
    private final Configuration jaywayConfig;
    @Autowired
    private OptionDataAnalyzer optionDataAnalyzer;
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    public NseManager(
            NseServiceProperties properties,
            NseScripTransformer nseScripTransformer,
            NseBulkDealTransformer nseBulkDealTransformer,
            @NseHome RestTemplate nseDataTemplate,
            Configuration jaywayConfig
    ) {
        this.properties = properties;
        this.nseScripTransformer = nseScripTransformer;
        this.nseBulkDealTransformer = nseBulkDealTransformer;
        this.nseDataTemplate = nseDataTemplate;
        this.jaywayConfig = jaywayConfig;
    }

    public ScripClient createNseClient() {
        return new NSEScripClient();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    class NSEScripClient implements ScripClient {
        @Value(NseConstants.PATH_SCRIP_RESPONSE_TRANSFORM_JSON)
        private Resource resource;
        @Value(NseConstants.PATH_COMPANY_DATA_JSON)
        private Resource scripSpec;

        private NSEScripClient() {
        }

        @Override
        public List<ScripData> getAll(Index index) throws IOException {
            ResponseEntity<String> responseEntity = nseDataTemplate.exchange(API_EQUITY_STOCK_INDICES, HttpMethod.GET, new HttpEntity<>(getHeaders(MediaType.APPLICATION_JSON_VALUE)), String.class, new HashMap<String, String>() {{
                put("index", index.getIndexValue());
            }});
            HttpStatus statusCode = responseEntity.getStatusCode();
            if (statusCode.is5xxServerError() || statusCode.is4xxClientError())
                throw new HttpResponseException(statusCode.value(), statusCode.getReasonPhrase());
            List<ScripData> scripData = nseScripTransformer.transform(responseEntity.getBody(), resource, new TypeReference<List<ScripData>>() {
            });
            return scripData;
        }

        @SneakyThrows
        @Override
        public void processAllScripsForIndex(List<String> excluded, Index index, int batchSize, Consumer<List<Scrip>> onEachBatchComplete) {
            ListUtils.partition(getAll(index), batchSize)
                    .parallelStream()
                    .peek(ohlcs -> System.out.println("Processing "+ohlcs.stream().map(ScripData::getScripName).collect(Collectors.joining(","))))
                    .map(ohlcVos -> ohlcVos.stream()
                            .map(scripData -> getScripData(scripData.getScripName()))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .peek(scrip -> scrip.setIndex(index))
                            .peek(scrip -> System.out.println("Processed -> " + scrip.getSymbol()))
                            .filter(scrip -> scrip.getSymbol() != null)
                            .collect(Collectors.toList()))
                    .forEach(onEachBatchComplete);
        }

        @SneakyThrows
        @Override
        public void processAllScrips(List<String> excluded, int batchSize, Consumer<List<Scrip>> onEachBatchComplete) {
            ListUtils.partition(bhavCopy().stream().filter(ohlc -> "EQ".equalsIgnoreCase(ohlc.getSeries().trim()) && !excluded.contains(ohlc.getSymbol())).parallel().collect(Collectors.toList()), batchSize)
                    .parallelStream()
                    .peek(ohlcs -> System.out.println("Processing "+ohlcs.stream().map(OHLC::getSymbol).collect(Collectors.joining(","))))
                    .map(ohlcVos -> ohlcVos.stream()
                            .map(ohlc -> getScripData(ohlc.getSymbol()))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .peek(scrip -> System.out.println("Processed -> " + scrip.getSymbol()))
                            .filter(scrip -> scrip.getSymbol() != null).collect(Collectors.toList()))
                    .forEach(onEachBatchComplete);
        }

        @SneakyThrows
        private Optional<Scrip> getScripData(String scrip) {
            try {
                ResponseEntity<String> responseEntity = nseDataTemplate.exchange(API_SCRIP_DATA, HttpMethod.GET, new HttpEntity<>(getHeaders(MediaType.APPLICATION_JSON_VALUE)), String.class, new HashMap<String, String>() {{
                    put("scripName", scrip);
                }});
                HttpStatus statusCode = responseEntity.getStatusCode();
                if (statusCode.is5xxServerError() || statusCode.is4xxClientError()) {
                    throw new HttpResponseException(statusCode.value(), statusCode.getReasonPhrase());
                }
                return Optional.of(nseScripTransformer.transform(responseEntity.getBody(), scripSpec, new TypeReference<Scrip>() {
                }));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }

        @Override
        public OHLC getTradeData(String scrip) throws IOException {
            System.out.println("Trying to fetch trade Info");
            return StockUtils.bhavCopy().stream().filter(ohlc -> scrip.equalsIgnoreCase(ohlc.getSymbol())).findFirst().get();
        }

        @Override
        public void getPastOHLCData(String scrip, Consumer<List<OHLCArchieve>> onBatch) throws IOException {
            StockUtils.fetchOHLCHistory(scrip, onBatch);
        }

        @Override
        public void getPastOHLCData(String scrip, LocalDate start, Consumer<List<OHLCArchieve>> onBatch) throws IOException {
            StockUtils.fetchOHLCHistory(scrip, start, onBatch);
        }

        @Override
        public void getPastOptionsData(String scrip, boolean isIndex, LocalDate start, String optionType, Consumer<List<DerivativeArchieve>> onBatch) throws IOException {
            StockUtils.fetchOptionsHistory(scrip, start, isIndex, optionType, onBatch);
        }

        @Override
        public void getPastOptionsData(String scrip, boolean isIndex, String optionType, Consumer<List<DerivativeArchieve>> onBatch) throws IOException {
            getPastOptionsData(scrip, isIndex, null, optionType, onBatch);
        }

        @Override
        public List<BulkDeal> getDeals(String scripName) throws IOException {
            ResponseEntity<String> responseEntity = nseDataTemplate.exchange(API_BULKDEAL_URL, HttpMethod.GET, new HttpEntity<>(getHeaders()), String.class, new HashMap<String, String>() {{
                put("scripName", scripName);
            }});
            HttpStatus statusCode = responseEntity.getStatusCode();
            if (statusCode.is5xxServerError() || statusCode.is4xxClientError())
                throw new HttpResponseException(statusCode.value(), statusCode.getReasonPhrase());
            return nseBulkDealTransformer.transform(responseEntity.getBody());
        }

        @Override
        public List<OptionChain> getOptionChain(String scripName) throws JsonProcessingException {
            System.out.println("Trying to fetch Options Chain");
            ResponseEntity<String> responseEntity = nseDataTemplate.exchange(NseConstants.API_OPTION_CHAIN_EQUITIES, HttpMethod.GET, new HttpEntity<>(getHeaders()), String.class, new HashMap<String, String>() {{
                put("scripName", scripName);
            }});
            return optionDataAnalyzer.getOptionChart(responseEntity.getBody());
        }

        @Override
        public String getOrderBook(String scrip){
            ResponseEntity<String> responseEntity = nseDataTemplate.exchange(API_SCRIP_DATA_TRADE_INFO, HttpMethod.GET, new HttpEntity<>(getHeaders(MediaType.APPLICATION_JSON_VALUE)), String.class, new HashMap<String, String>() {{
                put("scripName", scrip);
            }});
            return responseEntity.getBody();
        }
    }
}

