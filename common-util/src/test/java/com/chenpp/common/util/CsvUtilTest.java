package com.chenpp.common.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author April.Chen
 * @date 2024/6/18 09:51
 */
public class CsvUtilTest {

    @Test
    public void testReadCsv() {
        String charset = FileUtil.getCharset("/Users/chenpp/Downloads/文档审核问题240329-郇夏.xlsx");
        System.out.println(charset);
        CsvUtil.readCsv("/Users/chenpp/Downloads/文档审核问题240329-郇夏.xlsx");
//        CsvUtil.readCsv("/Users/chenpp/Desktop/云图/测试相关/信用卡样例图谱/客户表.csv");
    }

    @Test
    public void testWriteCsv() {
        String filePath = "/Users/chenpp/Downloads/test.csv";
//        String[] headers = {"姓名", "年龄"};
        String[] headers = null;
        List<List<Object>> records = Arrays.asList(
                Arrays.asList("张三", 18),
                Arrays.asList("李四", 19)
        );
        CsvUtil.writeCsv(filePath, records, headers);
    }
}