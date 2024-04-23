package com.chenpp.common.bigdata.hadoop;

import lombok.Data;

import java.util.Map;

/**
 * @author April.Chen
 * @date 2024/2/18 11:46
 */
@Data
public class HdfsConf {
    private String nameService;
    private Map<String, String> nameNodes;
    private String user;
    private String nameNode1;
    private String nameNode2;

}
