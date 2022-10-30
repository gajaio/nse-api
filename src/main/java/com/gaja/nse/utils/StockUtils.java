package com.gaja.nse.utils;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.gaja.nse.vo.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gaja.nse.utils.SecurityType.OPTIDX;
import static com.gaja.nse.utils.SecurityType.OPTSTK;
import static java.time.temporal.ChronoUnit.DAYS;

public class StockUtils {
    private static final int daysInBatch = 14;
    private static String NSE_INDIA_WWW1 = "https://www1.nseindia.com/";

    private StockUtils() {
        
    }

    public static LinkedMultiValueMap<String, String> getHeaders() {
        return getHeaders(null);
    }

    public static LinkedMultiValueMap<String, String> getHeaders(String accept) {
        return new LinkedMultiValueMap<String, String>() {{
            put("User-Agent", Collections.singletonList("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:76.0) Gecko/20100101 Firefox/76.0"));
            put("Connection", Collections.singletonList("keep-alive"));
            if (accept != null) put("Accept", Collections.singletonList("gzip, deflate, br, " + accept));
            else put("Accept", Collections.singletonList("gzip, deflate, br"));
            put("Accept-Encoding", Collections.singletonList("application/gzip"));
            put("Accept-Language", Collections.singletonList("en-US,en;q=0.5"));
        }};
    }

    public static List<ReportDaily> transformDeliveryData(String dataCsv) {
        Scanner lineScanner = new Scanner(new StringReader(dataCsv));
        IntStream.iterate(1, i -> i + 1).limit(4).forEach(i -> lineScanner.nextLine());
        List<ReportDaily> reportDailies = new ArrayList<>();
        lineScanner.forEachRemaining(nextLine -> {
            Scanner commaScanner = new Scanner(nextLine);
            commaScanner.useDelimiter(",");
            reportDailies.add(ReportDaily.builder()
                    .reportType(commaScanner.nextInt())
                    .slNum(commaScanner.nextInt())
                    .name(commaScanner.next())
                    .type(commaScanner.next())
                    .tradedQuantity(commaScanner.nextLong())
                    .deliverableQuantity(commaScanner.nextLong())
                    .percentage(commaScanner.nextDouble())
                    .build());
            commaScanner.close();
        });
        lineScanner.close();
        return reportDailies;
    }

    public static void fetchOHLCHistory(String scripName, Consumer<List<OHLCArchieve>> onBatch) throws IOException {
        fetchOHLCHistory(scripName,LocalDate.of(2017, 01, 01), onBatch);
    }
    public static void fetchOHLCHistory(String scripName, LocalDate fromDate, Consumer<List<OHLCArchieve>> onBatch) throws IOException {
        Connection.Response resp1 = Jsoup.connect(NSE_INDIA_WWW1 + "products/content/equities/equities/eq_security.htm")
                .followRedirects(false)
                .userAgent(NseConstants.MOZILLA_CLIENT)
                .method(Connection.Method.GET)
                .execute();
        if(fromDate == null) fromDate = LocalDate.now().minusMonths(23);
        long days = DAYS.between(fromDate, LocalDate.now().plusDays(1));
        long numBatches = days/ daysInBatch +(days% daysInBatch >0?1:0);
        LocalDate endDate = LocalDate.now();
        for (int i = 0; i < numBatches; i++) {
            endDate = fromDate.plusDays(days< daysInBatch ?days: daysInBatch);
            onBatch.accept(fetchEqData(scripName, fromDate, endDate, resp1));
            fromDate = endDate.plusDays(1);
        }

    }

    public static void fetchIndexHistory(LocalDate fromDate, Consumer<List<Index>> onBatch) throws IOException {
        Connection.Response resp1 = Jsoup.connect(NSE_INDIA_WWW1 + "products/content/equities/indices/historical_index_data.htm")
                .followRedirects(false)
                .userAgent(NseConstants.MOZILLA_CLIENT)
                .method(Connection.Method.GET)
                .execute();
        long days = DAYS.between(fromDate, LocalDate.now().plusDays(1));
        long numBatches = days/ 365 +(days% 365 >0?1:0);
        LocalDate endDate = LocalDate.now();
        for (int i = 0; i < numBatches; i++) {
            endDate = fromDate.plusDays(days< 364 ?days: 364);
            onBatch.accept(fetchIndex(fromDate, endDate, resp1));
            fromDate = endDate.plusDays(1);
        }
    }

    private static List<Index> fetchIndex(LocalDate fromDate, LocalDate toDate, Connection.Response resp1) throws IOException {
        Connection.Response response = Jsoup.connect(NSE_INDIA_WWW1 + "/products/dynaContent/equities/indices/historicalindices.jsp?indexType=" + URLEncoder.encode("NIFTY 50", StandardCharsets.UTF_8.toString()) + "&fromDate="+(fromDate !=null? fromDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")):"")+"&toDate="+(toDate !=null? toDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")): LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))))
                .userAgent(NseConstants.MOZILLA_CLIENT)
                .cookies(resp1.cookies())
                .referrer(NSE_INDIA_WWW1 + "products/content/equities/indices/historical_index_data.htm")
                .method(Connection.Method.GET)
                .execute();
        Document parsedResponse = response.parse();
        Element csvContentDiv = parsedResponse.getElementById("csvContentDiv");
        if(csvContentDiv==null) return new ArrayList<>();
        String csvData = csvContentDiv.text();
        CsvMapper mapper = new CsvMapper();
        CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader();
        ObjectReader oReader = mapper.readerFor(Index.class).with(bootstrapSchema);
        String formattedCsv = csvData.replaceAll("\"\\-\"", "\"0\"").replaceAll(":", "\n");
        formattedCsv = removeIrrelevantScripData(formattedCsv);
        MappingIterator<Index> ohlcItr = oReader.readValues(formattedCsv.getBytes());
        List<Index> indexArchieves = new ArrayList<>();
        ohlcItr.forEachRemaining(indexArchieves::add);
        return indexArchieves;
    }

    public static void fetchVixHistory(LocalDate fromDate, Consumer<List<Vix>> onBatch) throws IOException {
        Connection.Response resp1 = Jsoup.connect(NSE_INDIA_WWW1 + "products/content/equities/indices/historical_vix.htm")
                .followRedirects(false)
                .userAgent(NseConstants.MOZILLA_CLIENT)
                .method(Connection.Method.GET)
                .execute();
        long days = DAYS.between(fromDate, LocalDate.now().plusDays(1));
        long numBatches = days/ 365 +(days% 365 >0?1:0);
        LocalDate endDate = LocalDate.now();
        for (int i = 0; i < numBatches; i++) {
            endDate = fromDate.plusDays(days< 364 ?days: 364);
            onBatch.accept(fetchVix(fromDate, endDate, resp1));
            fromDate = endDate.plusDays(1);
        }
    }

    private static List<Vix> fetchVix(LocalDate fromDate, LocalDate toDate, Connection.Response resp1) throws IOException {
        Connection.Response response = Jsoup.connect(NSE_INDIA_WWW1 + "/products/dynaContent/equities/indices/hist_vix_data.jsp?fromDate="+(fromDate !=null? fromDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")):"")+"&toDate="+(toDate !=null? toDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")): LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))))
                .userAgent(NseConstants.MOZILLA_CLIENT)
                .cookies(resp1.cookies())
                .referrer(NSE_INDIA_WWW1 + "products/content/equities/indices/historical_vix.htm")
                .method(Connection.Method.GET)
                .execute();
        Document parsedResponse = response.parse();
        Element csvContentDiv = parsedResponse.getElementById("csvContentDiv");
        if(csvContentDiv==null) return new ArrayList<>();
        String csvData = csvContentDiv.text();
        CsvMapper mapper = new CsvMapper();
        CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader();
        ObjectReader oReader = mapper.readerFor(Vix.class).with(bootstrapSchema);
        String formattedCsv = csvData.replaceAll("\"\\-\"", "\"0\"").replaceAll(":", "\n");
        formattedCsv = removeIrrelevantScripData(formattedCsv);
        MappingIterator<Vix> ohlcItr = oReader.readValues(formattedCsv.getBytes());
        List<Vix> indexArchieves = new ArrayList<>();
        ohlcItr.forEachRemaining(indexArchieves::add);
        return indexArchieves;
    }

    private static List<OHLCArchieve> fetchEqData(String scripName, LocalDate fromDate, LocalDate toDate, Connection.Response resp1) throws IOException {
        Connection.Response resp = Jsoup.connect(NSE_INDIA_WWW1 + "marketinfo/sym_map/symbolCount.jsp?symbol=" + scripName)
                .followRedirects(false)
                .userAgent(NseConstants.MOZILLA_CLIENT)
                .cookies(resp1.cookies())
                .method(Connection.Method.GET)
                .execute();
        if(resp.body().trim().equals("0")) throw new RuntimeException("Nothing to fetch");
        Connection.Response response = Jsoup.connect(NSE_INDIA_WWW1 + "products/dynaContent/common/productsSymbolMapping.jsp?symbol=" + URLEncoder.encode(scripName, StandardCharsets.UTF_8.toString()) + "&segmentLink=3&symbolCount=" + resp.body().trim() + "&series=ALL&dateRange="+(fromDate !=null?"+":"24month")+"&fromDate="+(fromDate !=null? fromDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")):"")+"&toDate="+(toDate !=null? toDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")): LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))+"&dataType=PRICEVOLUMEDELIVERABLE")
                .userAgent(NseConstants.MOZILLA_CLIENT)
                .cookies(resp.cookies())
                .referrer(NSE_INDIA_WWW1 + "products/content/equities/equities/eq_security.htm")
                .method(Connection.Method.GET)
                .execute();
        Document parsedResponse = response.parse();
        System.out.println("Parsing "+ scripName);
        Element csvContentDiv = parsedResponse.getElementById("csvContentDiv");
        if(csvContentDiv==null) return new ArrayList<>();
        String csvData = csvContentDiv.text();
        CsvMapper mapper = new CsvMapper();
        CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader();
        ObjectReader oReader = mapper.readerFor(OHLCArchieve.class).with(bootstrapSchema);
        String formattedCsv = csvData.replaceAll("\"\\-\"", "\"0\"").replaceAll(":", "\n");
        formattedCsv = removeIrrelevantScripData(formattedCsv);
        MappingIterator<OHLCArchieve> ohlcItr = oReader.readValues(formattedCsv.getBytes());
        List<OHLCArchieve> ohlcArchieves = new ArrayList<>();
        ohlcItr.forEachRemaining(ohlcArchieves::add);
        return ohlcArchieves;
    }

    private static String removeIrrelevantScripData(String formattedCsv) {
        String[] splitted = formattedCsv.split("\n");
        return splitted[0]+"\n"+Arrays.stream(splitted).skip(1)/*.filter(s -> s.startsWith("\""+scripName+"\""))*/.reduce((s, s2) -> s+"\n"+s2).orElse("");
    }

    public static void fetchOptionsHistory(String scripName, LocalDate fromDate, boolean isIndex, String optionType, Consumer<List<DerivativeArchieve>> onBatch) throws IOException {
        Connection.Response resp1 = Jsoup.connect(NSE_INDIA_WWW1 + "products/content/derivatives/equities/historical_fo.htm")
                .followRedirects(false)
                .userAgent(NseConstants.MOZILLA_CLIENT)
                .method(Connection.Method.GET)
                .execute();
        long days = DAYS.between(fromDate, LocalDate.now());
        long numBatches = days/ daysInBatch +(days% daysInBatch >0?1:0);
        LocalDate endDate = LocalDate.now();
        for (int i = 0; i < numBatches; i++) {
            endDate = fromDate.plusDays(days< daysInBatch ?days: daysInBatch);
            onBatch.accept(fetchData(scripName, fromDate, endDate, isIndex, optionType, resp1));
            fromDate = endDate.plusDays(1);
        }
    }

    private static List<DerivativeArchieve> fetchData(String scripName, LocalDate fromDate, LocalDate toDate, boolean isIndex, String optionType, Connection.Response resp1) throws IOException {
        List<DerivativeArchieve> ohlcArchieves = new ArrayList<>();
        Connection.Response response = Jsoup.connect(NSE_INDIA_WWW1 + "products/dynaContent/common/productsSymbolMapping.jsp?instrumentType="+(isIndex ?OPTIDX.name():OPTSTK.name())+"&symbol=" + URLEncoder.encode(scripName, StandardCharsets.UTF_8.toString()) + "&expiryDate=select&strikePrice=&segmentLink=9&symbolCount=&optionType="+ optionType +"&dateRange="+(fromDate !=null?"+":"24month")+"&fromDate="+(fromDate !=null? fromDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")):"")+"&toDate="+(toDate !=null? toDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")):""))
                .userAgent(NseConstants.MOZILLA_CLIENT)
                .cookies(resp1.cookies())
                .referrer(NSE_INDIA_WWW1 + "products/content/derivatives/equities/historical_fo.htm")
                .method(Connection.Method.GET)
                .execute();
        Document parsedResponse = response.parse();
        System.out.println("Parsing "+ scripName);
        Element csvContentDiv = parsedResponse.getElementById("csvContentDiv");
        if(csvContentDiv==null) throw new RuntimeException("Nothing to fetch");
        String csvData = csvContentDiv.text();
        CsvMapper mapper = new CsvMapper();
        CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader();
        ObjectReader oReader = mapper.readerFor(DerivativeArchieve.class).with(bootstrapSchema);
        String formattedCsv = csvData.replaceAll("\"\\-\"", "\"0\"").replaceAll(":", "\n");
        MappingIterator<DerivativeArchieve> ohlcItr = oReader.readValues(formattedCsv.getBytes());
        ohlcItr.forEachRemaining(ohlcArchieves::add);
        return ohlcArchieves;
    }
    public static List<OHLC> bhavCopy() throws IOException {
        return bhavCopy(new ArrayList<>());
    }

    public static List<OHLC> bhavCopy(List<String> scrips) throws IOException {
        Connection.Response response = Jsoup.connect(NSE_INDIA_WWW1 + "products/content/sec_bhavdata_full.csv")
                .userAgent(NseConstants.MOZILLA_CLIENT)
                .ignoreContentType(true)
                .method(Connection.Method.GET)
                .execute();
        String csvData = response.body();

        CsvMapper mapper = new CsvMapper();
        CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader();
        ObjectReader oReader = mapper.readerFor(OHLC.class).with(bootstrapSchema);
        MappingIterator<OHLC> ohlcItr = oReader.readValues(csvData.replace(":", "\n").getBytes());
        List<OHLC> ohlcArchieves = new ArrayList<>();
        ohlcItr.forEachRemaining(ohlcArchieves::add);
        return scrips==null || scrips.isEmpty() ? ohlcArchieves:ohlcArchieves.stream().filter(ohlc -> scrips.contains(ohlc.getSymbol())).collect(Collectors.toList());
    }

    public static List<DerivativeArchieve> fetchDerivativeHistory(String scripName, SecurityType type, String optionType) throws IOException {
        Connection.Response resp1 = Jsoup.connect(NSE_INDIA_WWW1 + "products/content/derivatives/equities/historical_fo.htm")
                .followRedirects(false)
                .userAgent(NseConstants.MOZILLA_CLIENT)
                .method(Connection.Method.GET)
                .execute();
        Connection.Response response = Jsoup.connect(buildDerivativeHistoryUrl(scripName, type, optionType))
                .userAgent(NseConstants.MOZILLA_CLIENT)
                .timeout(3*1000)
                .maxBodySize(20*1024*1024)
                .cookies(resp1.cookies())
                .referrer(NSE_INDIA_WWW1 + "products/content/derivatives/equities/historical_fo.htm")
                .method(Connection.Method.GET)
                .execute();
//        System.out.println(response.body());
        Document parsedResponse = response.parse();
        String csvData = parsedResponse.getElementById("csvContentDiv").text();

        CsvMapper mapper = new CsvMapper();
        CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader();
        ObjectReader oReader = mapper.readerFor(DerivativeArchieve.class).with(bootstrapSchema);
        MappingIterator<DerivativeArchieve> ohlcItr = oReader.readValues(csvData.replace(":", "\n").replaceAll(",\"-\"", ",\"\"").getBytes());
        List<DerivativeArchieve> ohlcArchieves = new ArrayList<>();
        ohlcItr.forEachRemaining(ohlcArchieves::add);
        return ohlcArchieves;
    }

    private static String buildDerivativeHistoryUrl(String scripName, SecurityType type, String optionType) {
        return UriComponentsBuilder.fromHttpUrl(NSE_INDIA_WWW1 + "/products/dynaContent/common/productsSymbolMapping.jsp")
                .queryParams(new HttpHeaders() {{
                    put("instrumentType", Arrays.asList(type.name()));
                    put("symbol", Arrays.asList(scripName));
                    put("expiryDate", Arrays.asList("select"));
                    if (type == OPTIDX || type == OPTSTK)
                        put("optionType", Arrays.asList(optionType));
                    else put("optionType", Arrays.asList("select"));
                    put("strikePrice", Collections.emptyList());
                    put("dateRange", Arrays.asList("12month"));
                    put("fromDate", Collections.emptyList());
                    put("toDate", Collections.emptyList());
                    put("segmentLink", Arrays.asList("9"));
                    put("symbolCount", Collections.emptyList());
                }}).build().toUriString();
    }
}

interface Step{
    void execute(Connection.Response response, Consumer<String> onResponseBody) throws IOException;

    void execute(Consumer<String> onResponseBody) throws IOException;
}

class FetchStep implements Step{
    private Step step;
    private Connection connection;

    public FetchStep(Step step, Connection connection) {
        this.step = step;
        this.connection = connection;
    }
    public FetchStep(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void execute(Connection.Response response, Consumer<String> onResponseBody) throws IOException {
        Connection.Response executeResp = connection.cookies(response.cookies()).execute();
        if(step!=null) {
            step.execute(executeResp, onResponseBody);
        }else onResponseBody.accept(executeResp.body());
    }

    @Override
    public void execute(Consumer<String> onResponseBody) throws IOException {
        Connection.Response response = connection.execute();
        if(step!=null) step.execute(response, onResponseBody);
    }
}