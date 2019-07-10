package com.example.realpilot.utilAndConfig;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpConfig {
    private static final Logger log = LoggerFactory.getLogger(HttpConfig.class);

    private CloseableHttpClient httpClient;
    private HttpComponentsClientHttpRequestFactory httpRequestFactory;

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory httpRequestFactory) {
        return new RestTemplate(httpRequestFactory);
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(30000)         // 최대 오픈되는 커넥션 수 제한
                .setMaxConnPerRoute(3000)       // IP,포트 1쌍에 대해 수행 할 연결 수 제한
                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                .build();

        httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout(30000);
        httpRequestFactory.setReadTimeout(50000);
        httpRequestFactory.setHttpClient(httpClient);

        return httpRequestFactory;
    }
}