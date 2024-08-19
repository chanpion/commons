package com.chenpp.common.docker;

import lombok.Data;

/**
 * Docker连接配置
 *
 * @author April.Chen
 * @date 2023/9/27 2:39 下午
 **/
@Data
public class DockerProperties {

    /**
     * 是否启用
     */
    private Boolean enable;
    /**
     * Docker的地址，比如： tcp://localhost:2376 或者unix:///var/run/docker.sock
     */
    private String host;
    /**
     * Docker API版本，可通过docker version命令查看
     */
    private String apiVersion;
    /**
     * 是否开启 TLS 验证 (http 和 https 之间切换)
     */
    private Boolean tlsVerify;
    /**
     * TLS 验证的证书路径
     */
    private String tlsCertPath;
    /**
     * 下载源地址（docker镜像存放的地址）
     */
    private String registryUrl;
    /**
     * 登陆用户名 （推送镜像到docker云仓库时需要）
     */
    private String registryUsername;
    /**
     * 登陆用户密码（推送镜像到docker云仓库时需要）
     */
    private String registryPassword;
    /**
     * 登陆账户的邮箱（推送镜像到docker云仓库时需要）
     */
    private String registryEmail;
}
