package com.example.bankingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {

        var sslParams = new SSLParameters();
        sslParams.setEndpointIdentificationAlgorithm(null);

        var httpClient = HttpClient.newBuilder()
                .sslContext(createInsecureSslContext())
                .sslParameters(sslParams)
                .build();

        var requestFactory = new JdkClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("Authorization","Bearer sandbox.jwt.token")
                .build();
    }

    private SSLContext createInsecureSslContext() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[] { new X509TrustManager() {
                @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                @Override public void checkClientTrusted(java.security.cert.X509Certificate[] c, String t) {}
                @Override public void checkServerTrusted(java.security.cert.X509Certificate[] c, String t) {}
            }}, null);
            return ctx;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create insecure SSL context", e);
        }
    }
}
