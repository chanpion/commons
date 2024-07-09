package com.chenpp.common.bigdata.hive;

import com.chenpp.common.bigdata.security.KerberosConf;
import lombok.Data;

/**
 * @author April.Chen
 * @date 2023/11/6 4:43 下午
 **/
@Data
public class HiveConf {
    private String jdbcUrl;
    private String auth;
    private String username;
    private String password;

    private KerberosConf kerberosConf;
}
