package com.chenpp.common.bigdata.spark.demo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;

/**
 * @author April.Chen
 * @date 2024/4/28 10:30
 */
public class SparkDemo {

    public static void main(String[] args) throws IOException {
        Configuration configuration = new Configuration();

        // fix: Can't get Kerberos realm
        System.setProperty("java.security.krb5.realm", "");
        System.setProperty("java.security.krb5.kdc", "");
        System.out.println(UserGroupInformation.isLoginKeytabBased());


        SparkConf sparkConf = new SparkConf()
                .set("spark.hadoopRDD.ignoreEmptySplits", "false");

        SparkSession spark = SparkSession
                .builder()
                .appName("Spark Demo")
                .master("local[1]")
                .config(sparkConf)
                .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .config("spark.default.parallelism", "1")
                .config("spark.sql.shuffle.partitions", "1")
                .enableHiveSupport()
                .config("hive.metastore.uris", "thrift://yuntu-d-010057036018.te.td:9083")
                .getOrCreate();

        Dataset<Row> df = spark.sql("select * from cpp.person limit 10");
        df.printSchema();
        if (!df.isEmpty()) {
            df.show(10);
        }

        spark.close();
    }
}
