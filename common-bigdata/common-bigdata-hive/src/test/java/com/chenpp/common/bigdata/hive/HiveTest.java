package com.chenpp.common.bigdata.hive;

import com.chenpp.common.bigdata.security.KerberosConf;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;

/**
 * krb5Path: /Users/chenpp/bigdata/60/krb5.conf
 * keytabPath: /Users/chenpp/bigdata/60/admin.keytab
 *
 * @author April.Chen
 * @date 2024/7/3 17:57
 */
public class HiveTest {
    private HiveConf hiveConf;
    private HiveClient hiveClient;

    @Before
    public void setUp() throws Exception {
        System.setProperty("java.security.krb5.conf", "/Users/chenpp/bigdata/60/krb5.conf");
        hiveConf = new HiveConf();
//        hiveConf.setJdbcUrl("jdbc:hive2://10.57.36.18:10000");
//        hiveConf.setUsername("hive");
//        hiveConf.setPassword("hive");


        hiveConf.setAuth("KERBEROS");
        hiveConf.setJdbcUrl("jdbc:hive2://10.58.12.60:2181,10.58.12.61:2181,10.58.12.62:2181/default;principal=hive/_HOST@TEST.com;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2");

        KerberosConf kerberosConf = new KerberosConf();
        kerberosConf.setKerberosEnable(true);
        kerberosConf.setPrincipal("admin/admin");
        kerberosConf.setKrb5Path("/Users/chenpp/bigdata/60/krb5.conf");
        kerberosConf.setKeytabPath("/Users/chenpp/bigdata/60/admin.keytab");
        hiveConf.setKerberosConf(kerberosConf);

        hiveClient = new HiveClient(hiveConf);
    }

    @Test
    public void showTables() {
        List<String> tables = hiveClient.showTables("default");
        tables.forEach(System.out::println);
        tables.stream().parallel().forEach(System.out::println);
    }

    @Test
    public void testDescTable() throws Exception {
        HiveUtil.getHiveFields(hiveClient.getConnection(), "default.officer").forEach(System.out::println);
    }

    @Test
    public void testGetHiveField() throws Exception {
        Connection connection = HiveUtil.getConnection(hiveConf);
        HiveUtil.getHiveFields(connection, "default.officer").forEach(System.out::println);
        HiveUtil.getHiveFieldsBySql(connection, "select * from default.officer").forEach(System.out::println);

    }

    @Test
    public void testInsert() throws Exception {
        String sql = " insert into table  default.wls_nebula_new1_indicator_entity  partition(indicator_code='person_shortest_path', ds='20240417') values('9fd700c0534011ecadd07d1171d1cc12', '1')";
        Connection connection = HiveUtil.getConnection(hiveConf);
        HiveUtil.execute(connection, sql);
    }

}