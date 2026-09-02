package com.example.walletservice.properties;

import java.time.Duration;

public record RestClientProperties(String rootUri,
                                   int maxConnections,
                                   int maxConnectionsPerRoute,
                                   int inactivitySeconds,
                                   Duration readTimeout,
                                   Duration connectTimeout,
                                   Duration connectionRequestTimeout,
                                   boolean bufferingClientHttpRequestFactory,
                                   String keyStoreBundleName,
                                   String trustStoreBundleName,
                                   SslMode sslMode) {
}
