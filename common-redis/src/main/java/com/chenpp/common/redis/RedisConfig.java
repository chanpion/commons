package com.chenpp.common.redis;

import lombok.Data;

/**
 * @author April.Chen
 * @date 2023/4/25 10:00 上午
 **/
@Data
public class RedisConfig {

    private String host;
    private Integer port;
    private String password;
    private Integer database;
}
