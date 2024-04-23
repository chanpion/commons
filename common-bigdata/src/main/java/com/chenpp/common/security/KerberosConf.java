package com.chenpp.common.security;

import lombok.Data;

/**
 * Kerberos配置
 *
 * @author April.Chen
 * @date 2023/11/6 10:52 上午
 **/
@Data
public class KerberosConf {
    /**
     * 是否开启Kerberos认证
     */
    private boolean kerberosEnable;
    private String krb5Path;
    private String keytabPath;
    private String principal;
}
