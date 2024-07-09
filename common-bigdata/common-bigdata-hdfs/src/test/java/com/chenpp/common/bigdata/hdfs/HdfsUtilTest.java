package com.chenpp.common.bigdata.hdfs;

import com.chenpp.common.bigdata.security.KerberosConf;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author April.Chen
 * @date 2024/7/2 18:27
 */
public class HdfsUtilTest {
    private HdfsConf hdfsConf;
    private FileSystem fileSystem;

    @Before
    public void before() {
        hdfsConf = new HdfsConf();
        hdfsConf.setNameService("ns17");
        Map<String, String> nameNodes = new HashMap<>();
        nameNodes.put("nn1", "10.57.36.17:8020");
        nameNodes.put("nn2", "10.57.36.18:8020");

        hdfsConf.setNameNodes(nameNodes);
        fileSystem = HdfsUtil.getFileSystem(hdfsConf);
    }

    //    @Before
    public void initKerberosConf() {
        hdfsConf = new HdfsConf();
        hdfsConf.setNameService("ns60");
        Map<String, String> nameNodes = new HashMap<>();
        nameNodes.put("nn1", "10.58.12.60:8020");
        nameNodes.put("nn2", "10.58.12.61:8020");
        hdfsConf.setNameNodes(nameNodes);
        KerberosConf kerberosConf = new KerberosConf();
        kerberosConf.setKerberosEnable(true);
        kerberosConf.setPrincipal("admin/admin");
        kerberosConf.setKrb5Path("/Users/chenpp/bigdata/60/krb5.conf");
        kerberosConf.setKeytabPath("/Users/chenpp/bigdata/60/admin.keytab");
        hdfsConf.setKerberosConf(kerberosConf);
        hdfsConf.setNameNodePrincipal("nm/_HOST@TEST.com");
        hdfsConf.setDataNodePrincipal("dn/_HOST@TEST.com");
        fileSystem = HdfsUtil.getFileSystem(hdfsConf);
    }

    @Test
    public void testGetInstance() {
        FileSystem fs = HdfsUtil.getFileSystem(hdfsConf);
        Assert.assertNotNull(fileSystem);
        List<FileStatus> fileList = HdfsUtil.listFiles(fs, "/tmp");
        fileList.forEach(System.out::println);
    }


    @Test
    public void gestListFiles() {
        List<FileStatus> list = HdfsUtil.listFiles(fileSystem, "/");
        if (list != null) {
            list.forEach(System.out::println);
        }
    }

    @Test
    public void testExists() throws IOException {
        boolean exists = HdfsUtil.exists(fileSystem, "/cpp");
        System.out.println(exists);
    }

    @Test
    public void testMkdir() throws IOException {
        HdfsUtil.mkdir(fileSystem, "/cpp");
    }

    @Test
    public void testReadToString() {
        String content = HdfsUtil.readToString(fileSystem, "/客户风险特征.csv");
        System.out.println(content);
    }


    @Test
    public void testDelete() throws IOException {
        boolean delete = HdfsUtil.delete(fileSystem, "/cpp");
        System.out.println(delete);
    }

    @Test
    public void testUpload() throws IOException {
        String userDir = System.getProperty("user.dir");
        System.out.println(userDir);
        HdfsUtil.upload(fileSystem, userDir + File.separator + "客户风险特征.csv", "/cpp/客户风险特征.csv");
    }

    @Test
    public void testDownload() throws IOException {
        String userDir = System.getProperty("user.dir");
        System.out.println(userDir);
        HdfsUtil.download(fileSystem, "/客户风险特征.csv", userDir + File.separator + "客户风险特征.csv");
    }

    @Test
    public void testReadFile() {
        String path = "/客户风险特征.csv";
        try (FSDataInputStream inputStream = fileSystem.open(new Path(path))) {
            List<String> lines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
            System.out.println(lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}