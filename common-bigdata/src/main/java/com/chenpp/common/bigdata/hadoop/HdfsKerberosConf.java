package com.chenpp.common.bigdata.hadoop;

import com.chenpp.common.security.KerberosConf;
import lombok.Data;

/**
 * @author April.Chen
 * @date 2024/2/18 17:28
 */
@Data
public class HdfsKerberosConf extends KerberosConf {
    /**
     * 是否开启Kerberos认证
     */
    private boolean kerberosEnable;
    private String krb5Path;
    private String keytabPath;
    private String principal;
    private String nameNodePrincipal;
    private String dataNodePrincipal;
}
