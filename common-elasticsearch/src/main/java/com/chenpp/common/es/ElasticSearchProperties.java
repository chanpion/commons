package com.chenpp.common.es;

import lombok.ToString;

/**
 * @author pengpeng.chen
 * @date 2021/6/21
 */
@ToString
public class ElasticSearchProperties {
    public static final int DEFAULT_PORT = 9200;

    public ElasticSearchProperties() {
        this(null);
    }

    public ElasticSearchProperties(String hosts) {
        this.hosts = hosts;
        this.port = DEFAULT_PORT;
        this.maxConnectTotal = 100;
        this.maxConnectPerRoute = 100;
    }

    private String uri;
    /**
     * 地址，多个用逗号,隔开
     */
    private String hosts;
    /**
     * 端口号
     */
    private Integer port;
    /**
     * 最大连接数
     */
    private Integer maxConnectTotal;
    /**
     * 最大路由连接数
     */
    private Integer maxConnectPerRoute;
    /**
     * 用户名
     */
    private String user;
    /**
     * 密码
     */
    private String pass;
    /**
     * 是否使用 https 连接
     */
    private Boolean useHttps = false;

    /**
     * 是否信任所有 SSL 证书，生产环境请勿配置为 true
     */
    private Boolean ignoreSSLCert = false;

    public String getUri() {
        return uri;
    }

    public String getHosts() {
        return hosts;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getMaxConnectTotal() {
        return maxConnectTotal;
    }

    public int getMaxConnectPerRoute() {
        return maxConnectPerRoute;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public Boolean getUseHttps() {
        return useHttps;
    }

    public void setUseHttps(Boolean useHttps) {
        this.useHttps = useHttps;
    }

    public Boolean getIgnoreSSLCert() {
        return ignoreSSLCert;
    }

    public void setIgnoreSSLCert(Boolean ignoreSSLCert) {
        this.ignoreSSLCert = ignoreSSLCert;
    }

    public ElasticSearchProperties uri(String uri) {
        this.uri = uri;
        return this;
    }

    public ElasticSearchProperties hosts(String hosts) {
        this.hosts = hosts;
        return this;
    }

    public ElasticSearchProperties port(int port) {
        this.port = port;
        return this;
    }

    public ElasticSearchProperties maxConnectTotal(Integer maxConnectTotal) {
        this.maxConnectTotal = maxConnectTotal;
        return this;
    }

    public ElasticSearchProperties maxConnectPerRoute(int maxConnectPerRoute) {
        this.maxConnectPerRoute = maxConnectPerRoute;
        return this;
    }
}
