package com.chenpp.common.docker;

import lombok.Data;

import java.util.Map;

/**
 * Docker Swarm Service配置
 *
 * @author April.Chen
 * @date 2023/9/28 10:32 上午
 **/
@Data
public class DockerServiceProperties {

    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * service副本数，对应容器启动个数
     */
    private int replicas;
    private String endpointMode;
    private Integer endpointTargetPort;
    private String networkId;
    private Map<String, String> labels;
    private DockerContainerProperties containerProperties;
}
