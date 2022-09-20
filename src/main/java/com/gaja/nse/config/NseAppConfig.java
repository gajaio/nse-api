package com.gaja.nse.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaja.nse.annotations.NseArchive;
import com.gaja.nse.annotations.NseHome;
import com.gaja.nse.interceptor.NseInterceptor;
import com.gaja.nse.service.NseManager;
import com.gaja.nse.service.ScripClient;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.apache.http.HeaderElement;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.InvalidClassException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL;

@Configuration
@ConditionalOnClass(NseManager.class)
@ComponentScan("com.gaja.nse")
@EnableRetry
@EnableConfigurationProperties(NseServiceProperties.class)
public class NseAppConfig {

    @Autowired
    private NseServiceProperties properties;

    @Autowired
    private NseInterceptor nseInterceptor;

    @Autowired
    private NseManager nseManager;

    @Bean
    @ConditionalOnMissingBean
    public ScripClient scripClient(){
        return nseManager.createNseClient();
    }

    @Bean
    public RestTemplateBuilder builder(){
        return new RestTemplateBuilder();
    }

    @Bean
    @Scope("prototype")
    public PoolingHttpClientConnectionManager connectionManager(){
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setMaxTotal(5000);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(1000);
        return poolingHttpClientConnectionManager;
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public RequestConfig requestConfig(){
        return RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .setConnectionRequestTimeout(2000)
                .build();
    }

    @Bean
    @Scope("prototype")
    public CloseableHttpClient httpClient(){
        return HttpClients.custom()
                .setKeepAliveStrategy(keepAliveStrategy())
                .setConnectionManager(connectionManager())
                .setDefaultRequestConfig(requestConfig())
                .build();
    }

    //ensure closure of the unused connections with keep alive strategy
    @Bean
    @Scope("prototype")
    public ConnectionKeepAliveStrategy keepAliveStrategy(){
        return (httpResponse, httpContext) -> {
          //if header has timeout field, then return the time else return the default timeout
            BasicHeaderElementIterator headerIterator = new BasicHeaderElementIterator(httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE));
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(headerIterator, Spliterator.ORDERED), false)
                    .map(o -> (HeaderElement)o)
                    .filter(headerElement -> headerElement.getValue()!=null && headerElement.getName().equalsIgnoreCase("timeout"))
                    .findFirst()
                    .map(headerElement -> Long.parseLong(headerElement.getValue())*1000)
                    .orElseGet(() -> 20*1000L);
        };
    }



    @Bean
    @NseHome
    public RestTemplate nseRestTemplate() throws InvalidClassException {
        RestTemplate nseRestTemplate = builder()
                .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                .build();
        addhttpClient(nseRestTemplate.getRequestFactory());
        nseRestTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(properties.getHome()));
        nseRestTemplate.getInterceptors().add(nseInterceptor);
        return nseRestTemplate;
    }

    private void addhttpClient(ClientHttpRequestFactory factory) throws InvalidClassException {
        if(factory instanceof HttpComponentsClientHttpRequestFactory)
            ((HttpComponentsClientHttpRequestFactory)factory).setHttpClient(httpClient());
        else throw new InvalidClassException("required HttpComponentsClientHttpRequestFactory, but found "+factory.getClass().getCanonicalName());
    }

    @Bean
    @NseArchive
    public RestTemplate nseArchRestTemplate() throws InvalidClassException {
        RestTemplate nseRestTemplate = builder().requestFactory(HttpComponentsClientHttpRequestFactory.class)
                .build();
        addhttpClient(nseRestTemplate.getRequestFactory());
        nseRestTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(properties.getArchive()));
        return nseRestTemplate;
    }

    @Bean
    public com.jayway.jsonpath.Configuration jayWayConfiguration() {
        return com.jayway.jsonpath.Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonProvider())
                .mappingProvider(new JacksonMappingProvider())
                .addOptions(DEFAULT_PATH_LEAF_TO_NULL);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RetryTemplate retryTemplate() {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(100);

        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }
}
