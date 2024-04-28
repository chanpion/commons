package com.chenpp.common.bigdata.hbase;

import lombok.Data;

import java.util.List;

/**
 * @author April.Chen
 * @date 2024/4/26 18:00
 */
@Data
public class HbaseRow {
    private String rowKey;
    private List<HBaseCell> cells;
}
