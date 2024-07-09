package com.chenpp.common.bigdata.hive.pool;

import com.chenpp.common.bigdata.hive.HiveConf;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author April.Chen
 * @date 2024/7/4 10:19
 */
public class HivePoolTest {

    private HivePoolConfig hivePoolConfig = new HivePoolConfig();

    private HiveConf hiveConf;

    @Before
    public void setUp() throws Exception {
        System.setProperty("java.security.krb5.conf", "/Users/chenpp/bigdata/60/krb5.conf");
        hiveConf = new HiveConf();
        hiveConf.setJdbcUrl("jdbc:hive2://10.57.36.18:10000");
        hiveConf.setUsername("hive");
        hiveConf.setPassword("hive");
    }


    @Test
    public void testHivePool() throws Exception {
        HivePool hivePool = new HivePool(hiveConf, hivePoolConfig);
        Connection connection = hivePool.getConnection();
        try {
            String sql = "show databases";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        } finally {
            hivePool.releaseConnection(connection);
        }


    }
}