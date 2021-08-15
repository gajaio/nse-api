package com.gaja.nse.utils;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.gaja.nse.config.NseServiceProperties;
import com.gaja.nse.vo.OHLCArchieve;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@EnableConfigurationProperties(NseServiceProperties.class)
public class NSECookieHandler {
    private Map<String, List<String>> cookieMap;
    private NseServiceProperties properties;

    @Autowired
    public NSECookieHandler(NseServiceProperties properties) throws IOException {
        this.properties = properties;
        cookieMap = new ConcurrentHashMap<>();
        addCokies(properties);
    }

    private void addCokies(NseServiceProperties properties) throws IOException {
        Map<String, String> cookies = initialize(properties.getHome()).cookies();
        cookies.remove("bm_mi");
        cookieMap.put(properties.getHome(), cookies.keySet().stream().map(key -> key + "=" + cookies.get(key)).collect(Collectors.toList()));
    }

    public List<String> getHomePageCookie() {
        return cookieMap.get(properties.getHome());
    }

    public synchronized List<String> getCookie(String url) {
        return cookieMap.get(url);
    }

    public void saveCookie(String url, List<String> cookie) {
        cookieMap.put(url, cookie);
    }

    private Connection.Response initialize(String url) throws IOException {

        return Jsoup.connect(url)
                .userAgent(NseConstants.MOZILLA_CLIENT)
                .method(Connection.Method.GET)
                .followRedirects(false)
                .execute();
    }

    public synchronized void reInitialize(String failedUrl) throws IOException {
        addCokies(properties);
//        Map<String, String> cookies = initialize(properties.getHome())
//                .cookies();
//        cookies.remove("bm_mi");
//        cookieMap.put(properties.getHome(), cookies.keySet().stream().map(key -> key + "=" + cookies.get(key)).collect(Collectors.toList()));
        cookieMap.put(failedUrl, getHomePageCookie());
    }

}

