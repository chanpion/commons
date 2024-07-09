package com.chenpp.common.bigdata.hbase;

import com.chenpp.common.bigdata.security.LoginUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author April.Chen
 * @date 2024/2/18 11:21
 */
public class HBaseTest {

    private HBaseConf hbaseConf;

    private HBaseConf kerberosHBaseConf = new HBaseConf();

    private Connection connection;

    @Before
    public void init() throws IOException {
        hbaseConf = new HBaseConf();
        hbaseConf.setHbaseHost("10.57.36.17,10.57.36.18,10.57.36.19");
        hbaseConf.setHbasePort("2182");
        hbaseConf.setHbaseZnode("/hbase");
        hbaseConf.setKerberosEnable(false);


//        System.setProperty("java.security.krb5.realm", "");
//        System.setProperty("java.security.krb5.kdc", "");

//        kerberosHBaseConf.setHbaseHost("10.58.12.60,10.58.12.61,10.58.12.62");
//        kerberosHBaseConf.setHbasePort("2181");
//        kerberosHBaseConf.setHbaseZnode("/hbase-secure");
//        kerberosHBaseConf.setKerberosEnable(true);
//        kerberosHBaseConf.setMasterPrincipal("hbase/_HOST@TEST.comm");
//        kerberosHBaseConf.setRegionServerPrincipal("hbase/_HOST@TEST.comm");
//        kerberosHBaseConf.setPrincipal("hbase-cluster60");
//        kerberosHBaseConf.setKeytabPath("/Users/chenpp/bigdata/60/ark3/hbase.headless.keytab");
//        kerberosHBaseConf.setKrb5Path("/Users/chenpp/bigdata/60/ark3/krb5.conf");
//
//        LoginUtil.login(kerberosHBaseConf.getPrincipal(), kerberosHBaseConf.getKeytabPath(), kerberosHBaseConf.getKrb5Path(), new Configuration());

        connection = HBaseUtil.getConnection(hbaseConf);

    }

    @After
    public void after() {
        HBaseUtil.closeAllConnection();
    }

    @Test
    public void getConnection() throws IOException {
        Connection connection = HBaseUtil.getConnection(hbaseConf);
        System.out.println(connection.isClosed());
    }

    @Test
    public void testKerberosConnect() {
        try {
            Connection connection = HBaseUtil.getConnection(kerberosHBaseConf);
            System.out.println(connection.isClosed());
            Admin admin = connection.getAdmin();
            NamespaceDescriptor[] namespaceDescriptors = admin.listNamespaceDescriptors();
            for (NamespaceDescriptor namespaceDescriptor : namespaceDescriptors) {
                System.out.println(namespaceDescriptor.getName());
            }
            TableName[] tableNames = admin.listTableNamesByNamespace("default");
            for (TableName tableName : tableNames) {
                System.out.println(tableName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void connect() throws IOException {
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.property.clientPort", "2182");
        config.set("hbase.zookeeper.quorum", "10.57.36.17,10.57.36.18,10.57.36.19");
        //检查配置
        HBaseAdmin.available(config);
        Connection connection = ConnectionFactory.createConnection(config);
    }

    @Test
    public void tableExists() throws IOException {
        boolean exists = HBaseUtil.tableExists("zctest_indicator_entity", hbaseConf);
        System.out.println(exists);
    }

    @Test
    public void createTable() throws IOException {
        HBaseUtil.createTable(hbaseConf, "cpp:test", "c1", "c2");
    }

    @Test
    public void deleteTable() {
    }

    @Test
    public void testAdd() throws IOException {
        HBaseUtil.add(hbaseConf, "cpp:test", "row1", "c1", "name", "chenpp");
    }

    @Test
    public void deleteRow() throws IOException {
        HBaseUtil.deleteRow(hbaseConf, "cpp:test", "row1");
    }

    @Test
    public void queryFields() throws Exception {
        List<String> fields = HBaseUtil.queryFields("default:zctest_indicator_entity", hbaseConf);
        System.out.println(fields);
    }

    @Test
    public void testGetOneRow() throws Exception {
        HbaseRow data = HBaseUtil.getOneRow(connection, "zctest_indicator_entity", "107group1");
        System.out.println(data);
    }

    @Test
    public void queryByRowKeys() {

    }

    @Test
    public void scan() throws IOException {
        Set<HbaseRow> rows = HBaseUtil.scan(hbaseConf, "default:zctest_indicator_entity");
        rows.forEach(System.out::println);
    }


    public void testFilterQuery() throws IOException {
        Connection connection = HBaseUtil.getConnection(hbaseConf);
        // 1. 获取表
        Table waterBillTable = connection.getTable(TableName.valueOf("WATER_BILL"));
        // 2. 构建scan请求对象
        Scan scan = new Scan()
                .withStartRow(Bytes.toBytes("startRowxxx"))
                .withStopRow(Bytes.toBytes("StopRowxxx"));
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

        RowFilter filter = new RowFilter(CompareOperator.EQUAL, new BinaryComparator(Bytes.toBytes("224382618261914241")));
        // 3.2 构建过滤器列表
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL
                , startDateFilter
                , endDateFilter, filter);


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