package com.chenpp.common.bigdata.hdfs;

import com.chenpp.common.bigdata.security.KerberosConf;
import lombok.Data;

import java.util.Map;

/**
 * @author April.Chen
 * @date 2024/7/2 16:36
 */
@Data
public class HdfsConf {
    private String nameService;
    private Map<String, String> nameNodes;
    private String nameNodePrincipal;
    private String dataNodePrincipal;

    private KerberosConf kerberosConf;
}
