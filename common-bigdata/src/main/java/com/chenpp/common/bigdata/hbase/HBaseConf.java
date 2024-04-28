package com.chenpp.common.bigdata.hbase;

import lombok.Data;

/**
 * @author April.Chen
 * @date 2023/11/6 10:37 上午
 **/
@Data
public class HBaseConf {

    private String hbasePort;
    private String hbaseHost;
    private String hbaseZnode;
    private String hbaseNs = "default";
    private int hbaseRegionNum = 16;
    private String rootDir = "/hbase";
    private String regionServerPrincipal;
    private String masterPrincipal;
    private String krb5Path;
    private String keytabPath;
    private String principal;
    private Boolean kerberosEnable = false;
}
