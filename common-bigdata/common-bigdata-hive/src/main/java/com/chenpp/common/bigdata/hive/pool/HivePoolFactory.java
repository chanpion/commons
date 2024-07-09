package com.chenpp.common.bigdata.hive.pool;

import com.chenpp.common.bigdata.hive.HiveConf;
import com.chenpp.common.bigdata.hive.HiveUtil;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.sql.Connection;
import java.sql.SQLException;
/**
 * @author April.Chen
 * @date 2024/7/4 10:02
 */
public class HivePoolFactory extends BasePooledObjectFactory<Connection> {
    private final HiveConf hiveConf;

    public HivePoolFactory(HiveConf hiveConf) {
        this.hiveConf = hiveConf;
    }

    @Override
    public Connection create() throws Exception {
        return HiveUtil.getConnection(hiveConf);
    }

    @Override
    public PooledObject<Connection> wrap(Connection connection) {
        return new DefaultPooledObject<>(connection);
    }

    @Override
    public void destroyObject(PooledObject<Connection> p) throws Exception {
        p.getObject().close();
    }

    @Override
    public boolean validateObject(PooledObject<Connection> p) {
        try {
            // 设置一个非常短的超时，仅用于检查连接是否仍然可用
            return p.getObject().isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }
}
