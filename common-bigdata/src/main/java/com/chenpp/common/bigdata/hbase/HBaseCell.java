package com.chenpp.common.bigdata.hbase;

import lombok.Data;

/**
 * @author April.Chen
 * @date 2024/4/26 17:58
 */
@Data
public class HBaseCell {
    private String columnFamily;
    private String column;
    private String value;
}
