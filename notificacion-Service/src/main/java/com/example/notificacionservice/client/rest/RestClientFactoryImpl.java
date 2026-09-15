package com.example.notificacionservice.client.rest;

import com.example.notificacionservice.properties.RestClientProperties;
import com.example.notificacionservice.properties.SslMode;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.httpcomponents.hc5.PoolingHttpClientConnectionManagerMetricsBinder;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.observation.ClientRequestObservationConvention;
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RestClientFactoryImpl implements RestClientFactory {

    private final MeterRegistry meterRegistry;
    private final ClientRequestObservationConvention observationConvention;
    private final SslBundles sslBundles;

    public RestClientFactoryImpl(MeterRegistry meterRegistry,
                                 ObservationRegistry observationRegistry,
                                 SslBundles sslBundles) {
        this.meterRegistry = meterRegistry;
        this.sslBundles = sslBundles;
        this.observationConvention = new DefaultClientRequestObservationConvention();
    }

    @Override
    public RestClient create(RestClientProperties properties) {

        PoolingHttpClientConnectionManager cm = connectionManager(properties);
        new PoolingHttpClientConnectionManagerMetricsBinder(cm, properties.rootUri()).bindTo(meterRegistry);
        CloseableHttpClient httpClient = closeableHttpClient(cm, properties);
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestClient.Builder builder = RestClient.builder();

        return builder
                .requestInterceptor(new LoggingRestClientInterceptor())
                .requestFactory(requestFactory)
                .baseUrl(properties.rootUri())
                .build();
    }


    private CloseableHttpClient closeableHttpClient(PoolingHttpClientConnectionManager cm, RestClientProperties properties) {
        return HttpClients.custom()
                .useSystemProperties()
                .setConnectionManager(cm)
                .disableAutomaticRetries()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.of(properties.connectionRequestTimeout()))
                        .build())
                .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
                .build();
    }

    private PoolingHttpClientConnectionManager connectionManager(RestClientProperties properties) {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .useSystemProperties()
                .setTlsSocketStrategy(buildTlsSocketStrategy(properties))
                .setMaxConnTotal(properties.maxConnections())
                .setMaxConnPerRoute(properties.maxConnectionsPerRoute())
                .setDefaultSocketConfig(SocketConfig.custom()
                        .setSoTimeout(Timeout.of(properties.readTimeout()))
                        .build())
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.of(properties.connectTimeout()))
                        .setValidateAfterInactivity(properties.inactivitySeconds(), TimeUnit.SECONDS)
                        .build())
                .build();
    }

    private TlsSocketStrategy buildTlsSocketStrategy(RestClientProperties properties) {
        if (sslBundles != null && properties.sslMode() != null && properties.sslMode() != SslMode.NONE) {
            return new DefaultClientTlsStrategy(
                    buildSslContext(properties),
                    NoopHostnameVerifier.INSTANCE
            );
        }
        log.info("SSL configuration is skipped for RestClient with rootUri {}", properties.rootUri());
        return null;
    }

    private SSLContext buildSslContext(RestClientProperties properties) {
        log.info("Configuring SSL for RestClient with rootUri {}", properties.rootUri());
        try {
            SslMode sslMode = properties.sslMode();
            if (!StringUtils.hasText(properties.trustStoreBundleName())) {
                String errorMsg = "No trust store bundle specified.";
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
            SslBundle trustStoreBundle = sslBundles.getBundle(properties.trustStoreBundleName());
            if (trustStoreBundle == null) {
                String errorMsg = "Trust store bundle is not configured correctly";
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
            SSLContextBuilder sslContextBuilder = SSLContextBuilder.create()
                    .setProtocol(trustStoreBundle.getProtocol())
                    .loadTrustMaterial(trustStoreBundle.getStores().getTrustStore(), null);

            String keyStoreBundleName = properties.keyStoreBundleName();
            if (sslMode == SslMode.MTLS && StringUtils.hasText(keyStoreBundleName)) {
                SslBundle keyStoreBundle = sslBundles.getBundle(keyStoreBundleName);
                if (keyStoreBundle != null) {
                    sslContextBuilder.loadKeyMaterial(keyStoreBundle.getStores().getKeyStore(), null);
                }
            }
            return sslContextBuilder.build();
        } catch (Exception e) {
            log.error("Failed to build SSLContext. Reason: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


}
