package com.chenpp.common.docker;

import lombok.Builder;
import lombok.Data;

/**
 * @author April.Chen
 * @date 2024/1/9 14:27
 */
@Data
@Builder
public class ServiceContainerInfo {
    private String serviceName;
    private String serviceId;
    private String containerId;
    private String nodeId;
    private String nodeIp;
    private String state;
    private Integer port;
}
