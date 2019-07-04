package com.example.realpilot.utilAndConfig;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {
    /*
    private static final int CONNECT_TIMEOUT = 99999;
    private static final int CONNECTION_MANAGER_CONNECTION_REQUEST_TIMEOUT = 0;
    private static final int SOCKET_TIMEOUT = 5000;
    private static final int MAX_TOTAL = 200;
    private static final int MAX_PER_ROUTE = 200;

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(MAX_TOTAL);
        connectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);
        connectionManager.setValidateAfterInactivity(CONNECT_TIMEOUT);
        return connectionManager;
    }

    @Bean
    public RequestConfig requestConfig() {
        RequestConfig result = RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_MANAGER_CONNECTION_REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();
        return result;
    }

    @Bean
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager, RequestConfig requestConfig) {
        CloseableHttpClient result = HttpClientBuilder
                .create()
                .setConnectionManager(poolingHttpClientConnectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
        return result;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient(poolingHttpClientConnectionManager(), requestConfig()));

        //RestTemplate restTemplate = new RestTemplate();
        RestTemplate restTemplate = restTemplateBuilder.setReadTimeout(Duration.ofSeconds(15)).build();
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }
    */

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory httpRequestFactory) {
        return new RestTemplate(httpRequestFactory);
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(150)
                .setMaxConnPerRoute(50)
                .setConnectionTimeToLive(15000, TimeUnit.MICROSECONDS)
                .build();

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setHttpClient(httpClient);

        //RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
        //this.restTemplate = restTemplate;

        return httpRequestFactory;
    }
}

/*SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
        httpRequestFactory.setReadTimeout(5000);       //ms
        httpRequestFactory.setConnectTimeout(15000);   //ms
        */