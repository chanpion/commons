package com.chenpp.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * properties资源文件读取工具类
 *
 * @author April.Chen
 * @date 2023/4/24 3:01 下午
 **/
public class PropertyUtil {
    private static final Logger logger = LoggerFactory.getLogger(PropertyUtil.class);
    private static final String DEFAULT_PROPERTIES_FILE = " application.properties";
    private static Properties props;

    static {
        loadProps(DEFAULT_PROPERTIES_FILE);
    }

    synchronized static private void loadProps(String filename) {
        logger.info("开始加载properties文件内容.......");
        props = new Properties();
        try (InputStream in = PropertyUtil.class.getClassLoader().getResourceAsStream(filename)) {
            props.load(in);
        } catch (FileNotFoundException e) {
            logger.error("properties文件未找到", e);
        } catch (IOException e) {
            logger.error("出现IOException", e);
        }
        logger.info("加载properties文件内容完成...........");
        logger.info("properties文件内容：" + props);
    }

    /**
     * 读取key对应的value
     */
    public static String getProperty(String key) {
        if (null == props) {
            loadProps(DEFAULT_PROPERTIES_FILE);
        }
        return props.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        if (null == props) {
            loadProps(DEFAULT_PROPERTIES_FILE);
        }
        return props.getProperty(key, defaultValue);
    }
}