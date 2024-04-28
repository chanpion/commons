package com.chenpp.common.bigdata.hive;

import com.alibaba.fastjson.JSONObject;
import com.chenpp.common.bigdata.hadoop.HdfsKerberosConf;
import com.chenpp.common.security.LoginUtil;
import org.apache.hadoop.conf.Configuration;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author April.Chen
 * @date 2024/4/25 19:21
 */
public class HIveTest {
    private HiveConf hiveConf;
    private HiveJdbcClient hiveJdbcClient;
    private HdfsKerberosConf kerberosConf;

    @Before
    public void before() {
        kerberosConf = new HdfsKerberosConf();
        kerberosConf.setKerberosEnable(true);
        kerberosConf.setPrincipal("admin/admin@yuntu.com");
        kerberosConf.setKeytabPath("/Users/chenpp/bigdata/60/ark3/admin.keytab");
        kerberosConf.setKrb5Path("/Users/chenpp/bigdata/60/ark3/krb5.conf");
        kerberosConf.setNameNodePrincipal("nn/_HOST@yuntu.com");
        kerberosConf.setDataNodePrincipal("dn/_HOST@yuntu.com");

        hiveConf = new HiveConf();
        hiveConf.setJdbcUrl("jdbc:hive2://yuntu-qiye-e-010058012061.hz.td:10000/default;principal=hive/_HOST@yuntu.com");
//        hiveConf.setJdbcUrl("jdbc:hive2://yuntu-qiye-e-010058012062.hz.td:2181,yuntu-qiye-e-010058012060.hz.td:2181,yuntu-qiye-e-010058012061.hz.td:2181/default;principal=hive/_HOST@yuntu.com;auth=KERBEROS;user.principal=admin/admin@yuntu.com;user.keytab=/Users/chenpp/bigdata/60/ark3/admin.keytab");
        hiveConf.setUsername("hive");
        hiveConf.setPassword("hive");
        hiveConf.setAuth("KERBEROS");

        hiveJdbcClient = new HiveJdbcClient();
    }


    @Test
    public void testDatabases() throws Exception {
//        LoginUtil.setKrb5Config("/Users/chenpp/bigdata/60/ark3/krb5.conf");
        LoginUtil.login(kerberosConf.getPrincipal(), kerberosConf.getKeytabPath(), kerberosConf.getKrb5Path(), new Configuration());

        List<JSONObject> list = hiveJdbcClient.query(hiveConf, "show databases");
        list.forEach(System.out::println);

        System.out.println(hiveJdbcClient.showTables(hiveConf, "yuntu"));

        System.out.println(hiveJdbcClient.getHiveFieldByTable(hiveConf, "default.autoscene07284_indicator_entity"));
        System.out.println(hiveJdbcClient.getHiveFieldBySql(hiveConf, "select * from default.autoscene07284_indicator_entity"));
    }

    public void testInsert() {
        String sql = " insert into table  default.wls_nebula_new1_indicator_entity  partition(indicator_code='person_shortest_path', ds='20240417') values('9fd700c0534011ecadd07d1171d1cc12', '1')";
    }


    @Test
    public void testBaseAuth(){
        hiveConf = new HiveConf();
        hiveConf.setJdbcUrl("jdbc:hive2://10.57.36.18:10000/default");
        hiveConf.setUsername("hive");
        hiveConf.setPassword("hive");

        System.out.println(hiveJdbcClient.showTables(hiveConf, "yuntu"));
    }
}
