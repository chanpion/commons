package com.chenpp.common.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author April.Chen
 * @date 2024/4/18 16:51
 */
public class ElasticsearchClientFactory {
    private static Logger logger = LoggerFactory.getLogger(ElasticsearchClientFactory.class);

    public static ElasticsearchClient buildClient(ElasticSearchProperties properties) throws IOException {
        HttpHost[] httpHosts = parseHttpHosts(properties, properties.getUseHttps());
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (StringUtils.isNotEmpty(properties.getUser()) && StringUtils.isNotEmpty(properties.getPass())) {
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(properties.getUser(), properties.getPass()));
        }

        SSLIOSessionStrategy sslStrategy = null;
        if (properties.getUseHttps() && properties.getIgnoreSSLCert()) {
            sslStrategy = buildTrustAllSessionStrategy();
        }
        final SSLIOSessionStrategy sslioSessionStrategy = sslStrategy;

        // Create the low-level client
        RestClient restClient = RestClient.builder(httpHosts)
                .setHttpClientConfigCallback(httpClientConfigCallback -> httpClientConfigCallback
                        .setMaxConnTotal(properties.getMaxConnectTotal())
                        .setMaxConnPerRoute(properties.getMaxConnectPerRoute())
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setSSLStrategy(sslioSessionStrategy)
                )
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(5000)
                        .setSocketTimeout(60000)).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        // And create the API client
        ElasticsearchClient client = new ElasticsearchClient(transport);
        logger.info("elasticsearch info: {}", client.info());
        return client;
    }

    public static HttpHost[] parseHttpHosts(ElasticSearchProperties properties, boolean useHttps) {
        HttpHost[] httpHosts;
        String[] hosts = properties.getUri().split(",");
        httpHosts = Stream.of(hosts)
                .map(host -> {
                    String[] ipPort = host.split(":");
                    if (useHttps) {
                        return new HttpHost(ipPort[0], NumberUtils.toInt(ipPort[1], ElasticSearchProperties.DEFAULT_PORT), "https");
                    } else {
                        return new HttpHost(ipPort[0], NumberUtils.toInt(ipPort[1], ElasticSearchProperties.DEFAULT_PORT));
                    }
                })
                .collect(Collectors.toList())
                .toArray(new HttpHost[hosts.length]);
        return httpHosts;
    }


    public static SSLIOSessionStrategy buildTrustAllSessionStrategy() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (x509Certificates, s) -> {
                // trust all
                return true;
            }).build();
            return new SSLIOSessionStrategy(sslContext, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            throw new EsException("build ssl context for es client failed", e);
        }
    }
}
