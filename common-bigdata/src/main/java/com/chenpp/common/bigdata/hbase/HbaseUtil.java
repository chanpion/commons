package com.chenpp.common.bigdata.hbase;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chenpp.common.security.KerberosUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author April.Chen
 * @date 2023/11/6 10:34 上午
 **/
public class HbaseUtil {
    private final static Logger logger = LoggerFactory.getLogger(HbaseUtil.class);
    private static final Map<String, Connection> HBASE_CONN_POOL_MAP = new ConcurrentHashMap<>();
    private static final Configuration HBASE_CONFIG = HBaseConfiguration.create();

    public static Connection getConnection(HbaseConf hbaseConf) throws IOException {
        return connect(hbaseConf);
    }

    public static Connection connect(HbaseConf hbaseConf) throws IOException {
        String hbaseQuorum = Arrays.stream(hbaseConf.getHbaseHost().split(",")).map(host ->
                String.format("%s:%s", host, hbaseConf.getHbasePort())).collect(Collectors.joining(","));
        String hbaseZnode = hbaseConf.getHbaseZnode();

        String key = getKey(hbaseQuorum, hbaseZnode);
        Connection connection = HBASE_CONN_POOL_MAP.get(key);
        if (connection == null || connection.isClosed()) {
            synchronized (HBASE_CONN_POOL_MAP) {
                connection = HBASE_CONN_POOL_MAP.get(key);
                if (connection == null || connection.isClosed()) {
                    Configuration conf = HBaseConfiguration.create();
                    conf.set("hbase.zookeeper.quorum", hbaseQuorum);
                    conf.set("hbase.client.retries.number", "2");
                    conf.setInt("hbase.client.operation.timeout", 1000);
                    conf.set("zookeeper.recovery.retry", "2");
                    if (StringUtils.isNotEmpty(hbaseZnode)) {
                        conf.set("zookeeper.znode.parent", hbaseZnode);
                    }
                    if (hbaseConf.getKerberosEnable()) {
                        conf.set("hadoop.security.authentication", "kerberos");
                        conf.set("hbase.security.authentication", "kerberos");
                        conf.set("keytab.file", hbaseConf.getKeytabPath());
                        conf.set("kerberos.principal", hbaseConf.getPrincipal());
                        conf.set("hbase.regionserver.kerberos.principal", hbaseConf.getRegionServerPrincipal());
                        conf.set("hbase.master.kerberos.principal", hbaseConf.getMasterPrincipal());

                        //检查配置
                        HBaseAdmin.available(conf);
                        connection = ConnectionFactory.createConnection(conf, KerberosUtil.getAuthenticatedUser(hbaseConf.getKrb5Path(), hbaseConf.getKeytabPath(), hbaseConf.getPrincipal()));
                    } else {
                        //检查配置
                        HBaseAdmin.available(conf);
                        connection = ConnectionFactory.createConnection(conf);
                    }
                    HBASE_CONN_POOL_MAP.put(key, connection);
                }
            }
        }
        return connection;
    }

    private static String getKey(String zkQuorum, String hbaseZnode) {
        if (StringUtils.isEmpty(hbaseZnode)) {
            hbaseZnode = "";
        }
        return zkQuorum + hbaseZnode;
    }


    public static boolean tableExists(String tableName, HbaseConf hbaseConf) throws IOException {
        Connection connection = connect(hbaseConf);
        TableName tableNameWithNs = TableName.valueOf(String.format("%s:%s", hbaseConf.getHbaseNs(), tableName));
        return connection.getAdmin().tableExists(tableNameWithNs);
    }

    public static void createTable(HbaseConf hbaseConf, String table, String... columnFamily) throws IOException {
        Connection connection = connect(hbaseConf);
        Admin admin = connection.getAdmin();

        // 1. 判断表是否存在
        if (admin.tableExists(TableName.valueOf(table))) {
            return;
        }

        // 2. 构建表描述构建器
        TableDescriptorBuilder tableDescriptorBuilder = TableDescriptorBuilder.newBuilder(TableName.valueOf(table));
        List<ColumnFamilyDescriptor> columnFamilyDescriptors = Arrays.stream(columnFamily)
                .map(cf -> ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(cf)).build())
                .collect(Collectors.toList());
        // 3. 添加列蔟
        tableDescriptorBuilder.setColumnFamilies(columnFamilyDescriptors);
        TableDescriptor tableDescriptor = tableDescriptorBuilder.build();

        // 4. 创建表
        admin.createTable(tableDescriptor);
    }

    public static void deleteTable(HbaseConf hbaseConf, String tableName) throws IOException {
        Connection connection = connect(hbaseConf);
        Admin admin = connection.getAdmin();
        TableName table = TableName.valueOf(tableName);
        // 1. 判断表是否存在
        if (admin.tableExists(table)) {
            // 2. 禁用表
            admin.disableTable(table);
            // 3. 删除表
            admin.deleteTable(table);
        }
        admin.close();
    }

    public static void add(HbaseConf hbaseConf, String tableName, String rowKey, String columnFamily, String column, String value) throws IOException {
        Connection connection = connect(hbaseConf);
        Table table = connection.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(Bytes.toBytes(columnFamily)
                , Bytes.toBytes(column)
                , Bytes.toBytes(value));
        table.put(put);
        table.close();
    }

    public static void add(HbaseConf hbaseConf, String tableName, String rowKey, String columnFamily, Map<String, Object> data) throws IOException {
        Connection connection = connect(hbaseConf);
        Table table = connection.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(rowKey));
        data.forEach((k, v) -> put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(k), Bytes.toBytes(v.toString())));
        table.put(put);
        table.close();
    }

    public static void deleteRow(HbaseConf hbaseConf, String tableName, String rowKey) throws IOException {
        Connection connection = connect(hbaseConf);
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            table.delete(delete);
        }
    }

    public static List<String> queryFields(String tableNameWithNs, HbaseConf hbaseConf) throws Exception {
        try {
            Connection connection = connect(hbaseConf);
            Table table = connection.getTable(TableName.valueOf(tableNameWithNs));

            Scan scan = new Scan();
            //todo workaround
            scan.setMaxResultSize(5 * 1024);
            scan.setOneRowLimit();

            ResultScanner rs = table.getScanner(scan);

            Set<String> res = new HashSet<>();
            for (Result r : rs) {
                List<String> qualifier = r.listCells().stream()
                        .map(cell -> Bytes.toString(CellUtil.cloneQualifier(cell)))
                        .collect(Collectors.toList());
                res.addAll(qualifier);
            }
            return new ArrayList<>(res);
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    public JSONObject queryByRowKey(String tableName, String rowKey, HbaseConf hbaseConf) throws Exception {
        JSONObject res = new JSONObject();

        if (StringUtils.isBlank(rowKey) || StringUtils.isEmpty(hbaseConf.getHbaseHost()) || StringUtils.isEmpty(hbaseConf.getHbasePort())) {
            return null;
        }

        Connection connection = connect(hbaseConf);
        if (connection == null) {
            return null;
        }

        TableName queryTable = TableName.valueOf(String.format("%s:%s", hbaseConf.getHbaseNs(), tableName));

        try (Admin admin = connection.getAdmin()) {
            if (!admin.tableExists(queryTable)) {
                logger.info("hbase table does not exist.,table name:{}", queryTable);
                return res;
            }
        }

        try (Table table = connection.getTable(queryTable)) {
            Get get = new Get(rowKey.getBytes());

            Result result = table.get(get);

            NavigableMap<byte[], byte[]> qualifierNValue = result.getFamilyMap(Bytes.toBytes("c"));

            if (qualifierNValue == null) {
                return res;
            }
            qualifierNValue.keySet().forEach(qualifier -> {
                String value = Bytes.toString(qualifierNValue.get(qualifier));
                if (StringUtils.isNotEmpty(value)) {
                    res.put(Bytes.toString(qualifier), value);
                }
            });

            // 4. 获取所有单元格
            List<Cell> cellList = result.listCells();

            // 打印rowkey
            System.out.println("rowkey => " + Bytes.toString(result.getRow()));

            // 5. 迭代单元格列表
            for (Cell cell : cellList) {
                // 打印列蔟名
                System.out.print(Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()));
                System.out.println(" => " + Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));

            }
        }
        return res;
    }

    /**
     * 根据多个rowkey查询
     *
     * @param tableName
     * @param rowKeys
     * @return
     * @throws Exception
     */
    public JSONArray queryByRowKeys(String workspaceUuid, String tableName, List<String> rowKeys, HbaseConf hbaseConf) throws Exception {
        JSONArray res = new JSONArray();

        if (CollectionUtils.isEmpty(rowKeys)) {
            return null;
        }

        if (StringUtils.isEmpty(hbaseConf.getHbaseHost()) || StringUtils.isEmpty(hbaseConf.getHbasePort())) {
            return null;
        }

        Connection connection = connect(hbaseConf);
        if (connection == null) {
            return null;
        }
        TableName queryTable = TableName.valueOf(String.format("%s:%s", hbaseConf.getHbaseNs(), tableName));

        try (Admin admin = connection.getAdmin()) {
            if (!admin.tableExists(queryTable)) {
                logger.info("hbase table does not exist.,table name:{}", queryTable);
                return res;
            }
        }

        try (Table table = connection.getTable(queryTable)) {
            List<Get> getList = new ArrayList<>();
            rowKeys.forEach(rk -> {
                if (StringUtils.isNotBlank(rk)) {
                    Get get = new Get(rk.getBytes());
                    getList.add(get);
                }
            });

            Result[] results = table.get(getList);

            if (results == null || results.length == 0) {
                return res;
            }

            for (int i = 0; i < results.length; i++) {
                Result result = results[i];
                NavigableMap<byte[], byte[]> qualifierNValue = result.getFamilyMap(Bytes.toBytes("c"));
                if (qualifierNValue != null) {
                    Map<String, String> obj = new HashMap<>();
                    qualifierNValue.keySet().forEach(qualifier -> {
                        String value = Bytes.toString(qualifierNValue.get(qualifier));
                        if (StringUtils.isNotEmpty(value)) {
                            obj.put(Bytes.toString(qualifier), value);
                        }
                    });
                    res.add(obj);
                }
            }
        }
        return res;
    }

    public static void scan(String tableName) throws IOException {
        Configuration conf = HBaseConfiguration.create(HBASE_CONFIG);
        Connection connection = ConnectionFactory.createConnection(HBASE_CONFIG, User.create(UserGroupInformation.getLoginUser()));
        Table table = connection.getTable(TableName.valueOf(tableName));

        System.out.println("scan");
        Scan scan = new Scan();
        scan.setOneRowLimit();
        //todo workaround
        scan.setMaxResultSize(5 * 1024);

        ResultScanner rs = table.getScanner(scan);
        Set<String> res = new HashSet<>();
        for (Result r : rs) {
            List<String> qualifier = r.listCells().stream()
                    .map(cell -> Bytes.toString(CellUtil.cloneQualifier(cell)))
                    .collect(Collectors.toList());
            res.addAll(qualifier);
        }
        System.out.println(res);

        HColumnDescriptor columnDescriptor = new HColumnDescriptor("");
        columnDescriptor.setBloomFilterType(BloomType.ROW);
    }

    public static void getRow(String tableOfName) throws java.lang.Exception {
        Configuration conf = HBaseConfiguration.create(HBASE_CONFIG);
        Connection connection = ConnectionFactory.createConnection(HBASE_CONFIG, User.create(UserGroupInformation.getLoginUser()));

        TableName tableName = TableName.valueOf(tableOfName);
        //取得一个要操作的表
        Table table = connection.getTable(tableName);

        //设置要查询的行的rowkey
        Get wangwu = new Get(Bytes.toBytes("wangwu"));

        //设置显示多少个版本的数据
        wangwu.setMaxVersions(3);

        //取得指定时间戳的数据
        //wangwu.setTimeStamp(1);

        //限制要显示的列族
        wangwu.addFamily(Bytes.toBytes("grade"));
        //限制要显示的列
        //wangwu.addColumn(Bytes.toBytes("course"), Bytes.toBytes("yuwen"));
        Result result = table.get(wangwu);
        List<Cell> cells = result.listCells();
        for (Cell c : cells) {
            //注意这里 CellUtil类的使用
            System.out.print("行键:" + Bytes.toString(CellUtil.cloneRow(c)) + " ");
            System.out.print("列族:" + Bytes.toString(CellUtil.cloneFamily(c)) + " ");
            System.out.print("列:" + Bytes.toString(CellUtil.cloneQualifier(c)) + " ");
            System.out.print("值:" + Bytes.toString(CellUtil.cloneValue(c)) + " ");
            System.out.println();
        }

        //关闭资源
        table.close();
        //hbaseConn.close();
    }

    public static void getMultiRows(String tableOfName) throws java.lang.Exception {
        Configuration conf = HBaseConfiguration.create(HBASE_CONFIG);
        Connection connection = ConnectionFactory.createConnection(HBASE_CONFIG, User.create(UserGroupInformation.getLoginUser()));

        TableName tableName = TableName.valueOf(tableOfName);
        //取得一个要操作的表
        Table table = connection.getTable(tableName);


        ArrayList<Get> getArrayList = new ArrayList<>();

        Get wangwu = new Get(Bytes.toBytes("wangwu"));
        //限制要显示的列族
        wangwu.addFamily(Bytes.toBytes("grade"));
        //限制要显示的列
        wangwu.addColumn(Bytes.toBytes("course"), Bytes.toBytes("yuwen"));

        Get lishi = new Get(Bytes.toBytes("lishi"));

        getArrayList.add(wangwu);
        getArrayList.add(lishi);

        Result[] results = table.get(getArrayList);
        for (int i = 0; i < results.length; i++) {
            Result result = results[i];
            List<Cell> cells = result.listCells();
            for (Cell c : cells) {
                //注意这里 CellUtil类的使用
                System.out.print("行键:" + Bytes.toString(CellUtil.cloneRow(c)) + " ");
                System.out.print("列族:" + Bytes.toString(CellUtil.cloneFamily(c)) + " ");
                System.out.print("列:" + Bytes.toString(CellUtil.cloneQualifier(c)) + " ");
                System.out.print("值:" + Bytes.toString(CellUtil.cloneValue(c)) + " ");
                System.out.println();
            }
        }
        //关闭资源
        table.close();
        //hbaseConn.close();
    }

    public static void showData(Result result) {
        while (result.advance()) {
            Cell cell = result.current();
            String row = Bytes.toString(CellUtil.cloneRow(cell));
            String cf = Bytes.toString(CellUtil.cloneFamily(cell));
            String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));
            String val = Bytes.toString(CellUtil.cloneValue(cell));
            System.out.println(row + "--->" + cf + "--->" + qualifier + "--->" + val);
        }
    }

    public static void stringFilter(HbaseConf hbaseConf) throws IOException {
        Connection connection = connect(hbaseConf);
        //每个线程使用单独的Table对象
        Table table = connection.getTable(TableName.valueOf("test"));

        Configuration conf = HBaseConfiguration.create();


        RowFilter filter = new RowFilter(CompareOperator.EQUAL, new BinaryComparator(Bytes.toBytes("224382618261914241")));
        // 构建Scan
        Scan scan = new Scan()
                .withStartRow(Bytes.toBytes("startRowxxx"))
                .withStopRow(Bytes.toBytes("StopRowxxx"))
                .setFilter(filter);

        // 获取resultScanner
        ResultScanner scanner = table.getScanner(scan);
        Result result = null;

        // 处理结果
        while ((result = scanner.next()) != null) {
            byte[] value = result.getValue(Bytes.toBytes("ship"), Bytes.toBytes("addr"));
            if (value == null || value.length == 0) {
                continue;
            }
            System.out.println(new String(value));
        }

        // 关闭ResultScanner
        scanner.close();
        table.close();
    }

    public void queryTest1(HbaseConf hbaseConf) throws IOException {
        Connection connection = connect(hbaseConf);
        // 1. 获取表
        Table waterBillTable = connection.getTable(TableName.valueOf("WATER_BILL"));
        // 2. 构建scan请求对象
        Scan scan = new Scan();
        // 3. 构建两个过滤器
        // 3.1 构建日期范围过滤器（注意此处请使用RECORD_DATE——抄表日期比较
        SingleColumnValueFilter startDateFilter = new SingleColumnValueFilter(Bytes.toBytes("C1")
                , Bytes.toBytes("RECORD_DATE")
                , CompareOperator.GREATER_OR_EQUAL
                , Bytes.toBytes("2020-06-01"));

        SingleColumnValueFilter endDateFilter = new SingleColumnValueFilter(Bytes.toBytes("C1")
                , Bytes.toBytes("RECORD_DATE")
                , CompareOperator.LESS_OR_EQUAL
                , Bytes.toBytes("2020-06-30"));

        // 3.2 构建过滤器列表
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL
                , startDateFilter
                , endDateFilter);

        scan.setFilter(filterList);

        // 4. 执行scan扫描请求
        ResultScanner resultScan = waterBillTable.getScanner(scan);

        // 5. 迭代打印result
        for (Result result : resultScan) {
            System.out.println("rowkey -> " + Bytes.toString(result.getRow()));
            System.out.println("------");

            List<Cell> cellList = result.listCells();

            // 6. 迭代单元格列表
            for (Cell cell : cellList) {

                String qualifier = new String(CellUtil.cloneQualifier(cell));
                String value = new String(CellUtil.cloneValue(cell), "UTF-8");

                // 打印列蔟名
                System.out.print(Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()));
                System.out.println(" => " + Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));

            }
            System.out.println("------");
        }

        resultScan.close();


        // 7. 关闭表
        waterBillTable.close();
    }

}
