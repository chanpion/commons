package com.chenpp.common.security;

import lombok.Data;

/**
 * @author April.Chen
 * @date 2023/11/6 10:52 上午
 **/
@Data
public class KerberosConf {
    private String krb5Path;
    private String keytabPath;
    private String principal;
}
