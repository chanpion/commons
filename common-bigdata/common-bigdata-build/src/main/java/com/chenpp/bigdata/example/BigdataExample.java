package com.chenpp.bigdata.example;

import com.alibaba.fastjson.JSON;
import com.chenpp.common.bigdata.hbase.HBaseConf;
import com.chenpp.common.bigdata.hbase.HBaseUtil;
import com.chenpp.common.bigdata.hdfs.HdfsConf;
import com.chenpp.common.bigdata.hdfs.HdfsUtil;
import com.chenpp.common.bigdata.hive.HiveConf;
import com.chenpp.common.bigdata.hive.HiveUtil;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

/**
 * @author April.Chen
 * @date 2024/7/5 17:27
 */
public class BigdataExample {

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.out.println("请输入参数");
            return;
        }

        switch (args[0]) {
            case "hdfs":
                hdfsExample(args[1]);
                break;
            case "hive":
                hiveExample(args[1]);
                break;
            case "hbase":
                hbaseExample(args[1]);
                break;
            default:
                System.out.println("请输入正确的参数");
                break;
        }
    }

    public static void hdfsExample(String arg) {
        HdfsConf hdfsConf = JSON.parseObject(arg, HdfsConf.class);
        FileSystem fs = HdfsUtil.getFileSystem(hdfsConf);
        List<FileStatus> files = HdfsUtil.listFiles(fs, "/tmp");
        System.out.println("/tmp files:");
        files.forEach(System.out::println);
    }

    public static void hiveExample(String arg) {
        HiveConf hiveConf = JSON.parseObject(arg, HiveConf.class);
        try {
            Connection connection = HiveUtil.getConnection(hiveConf);
            HiveUtil.showDatabases(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hbaseExample(String arg) {
        HBaseConf hbaseConf = JSON.parseObject(arg, HBaseConf.class);
        try {
            List<String> namespaces = HBaseUtil.listNamespaces(hbaseConf);
            System.out.println("namespace:");
            namespaces.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
