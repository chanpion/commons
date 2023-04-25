package com.chenpp.common.redis;

import com.alibaba.fastjson.JSONObject;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * @author April.Chen
 * @date 2023/4/25 9:57 上午
 **/
public class LettuceUtil {
    private static RedisConfig redisConfig = new RedisConfig();

    /**
     * 获取command
     *
     * @return
     */
    private static RedisURI getRedisUri() {
        String host = redisConfig.getHost();
        Integer port = redisConfig.getPort();
        String password = redisConfig.getPassword();
        Integer database = redisConfig.getDatabase();
        RedisURI redisUri = RedisURI.Builder.redis(host)
                .withPort(port)
                .withPassword(password)
                .withDatabase(database)
                .build();
        return redisUri;
    }

    /**
     * 匹配key 返回匹配的key列表
     *
     * @param keys
     * @return
     */
    public static List<String> keys(String keys) {
        RedisURI redisUri = getRedisUri();
        RedisClient client = RedisClient.create(redisUri);
        StatefulRedisConnection<String, String> connect = client.connect();
        RedisCommands<String, String> commands = connect.sync();
        List<String> keyList = commands.keys(keys);
        connect.close();
        client.shutdown();
        return keyList;
    }

    /**
     * 设置key,和value 失效时间为一天
     *
     * @param key
     * @param value
     */
    public static void set(String key, String value) {
        RedisURI redisUri = getRedisUri();
        RedisClient client = RedisClient.create(redisUri);
        StatefulRedisConnection<String, String> connect = client.connect();
        RedisCommands<String, String> commands = connect.sync();
        commands.setex(key, 360, value);
        connect.close();
        client.shutdown();
    }

    /**
     * 设置key,和value 失效时间为一天
     *
     * @param key
     * @param value
     */
    public static void set(String key, Object value) {
        String valueStr = null;
        if (value != null) {
            valueStr = JSONObject.toJSONString(value);
        }
        set(key, valueStr);
    }

    public static String get(String key) {
        RedisURI redisUri = getRedisUri();
        RedisClient client = RedisClient.create(redisUri);
        StatefulRedisConnection<String, String> connect = client.connect();
        RedisCommands<String, String> commands = connect.sync();
        String s = commands.get(key);
        connect.close();
        client.shutdown();
        return s;
    }

    private static class Helper {
        private static final String KEY_SCHEMA = "";
        static String memberSrl = "";
        static Long ttl = 0L;
        static RedisClusterClient redisClient;

        static StatefulRedisClusterConnection<String, String> connection;

        static final MessageFormat KEY_FORMAT = new MessageFormat(KEY_SCHEMA);

        static {

            RedisURI uri = RedisURI.builder()
                    .withHost(redisConfig.getHost())
                    .withPort(redisConfig.getPort())
                    .withPassword(redisConfig.getPassword())
                    .withSsl(true)
                    .build();


            redisClient = RedisClusterClient.create(uri);
            connection = redisClient.connect();

            System.out.println("==> Connected to Redis");

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (connection != null) {
                        connection.close();
                    }
                    if (redisClient != null) {
                        redisClient.shutdown();
                    }
                }
            });
        }

    }

    public static String getKey(String memberSrl, String value) {
        return Helper.KEY_FORMAT.format(new String[]{memberSrl, value});
    }

    /**
     * save the expiration value if more than stored value.
     *
     * @param key        key
     * @param value      value
     * @param expireTime expire time in milliseconds.
     */
    public static String set(String key, String value, long expireTime) {

        RedisAdvancedClusterCommands<String, String> commands = Helper.connection.sync();

//        String oldKey = getKey(memberSrl, value);
//        String oldExpire = commands.get(key);

        return commands.psetex(key, 10L, expireTime + "");

    }

    /**
     * set all key values, You should ensure that the expiration time
     * should be greater than the value that redis has stored. <br />
     * key from KEY_FORMAT.format(memberSrl, uuid/pcid/ip/fp) <br />
     *
     * @param keyVals
     * @return
     */
    public static String sets(Map<String, String> keyVals) {
        if (keyVals == null || keyVals.isEmpty()) {
            return "Empty Parameters";
        }

        return Helper.connection.sync().mset(keyVals);
    }

    /**
     * get the expiration time of input key.
     *
     * @param memberSrl
     * @param value
     * @return
     */
    public static Long get(String memberSrl, String value) {
        String key = getKey(memberSrl, value);
        String expire = Helper.connection.sync().get(key);
        return (expire == null) ? 0 : Long.parseLong(expire);
    }

    /**
     * input many keys and then query all
     *
     * @param keys
     * @return
     */
    public static List<KeyValue<String, String>> gets(String... keys) {
        return Helper.connection.sync().mget(keys);
    }

    /**
     * @param memberSrl
     * @param categoryValue
     * @return
     */
    public static Long del(String memberSrl, String categoryValue) {
        String key = getKey(memberSrl, categoryValue);
        return Helper.connection.sync().del(key);
    }

    /**
     * delete many keys, key from KEY_FORMAT.format(memberSrl, uuid/pcid/ip/fp);
     *
     * @param keys
     * @return
     */
    public static Long dels(String... keys) {
        if (keys == null || keys.length == 0) {
            return 0L;
        }

        return Helper.connection.sync().del(keys);
    }

    /**
     * get lettuce connection, you can do command on it.
     *
     * @return
     */
    public static StatefulRedisClusterConnection getConnection() {
        return Helper.connection;
    }


}
