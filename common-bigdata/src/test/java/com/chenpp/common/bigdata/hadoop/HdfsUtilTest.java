package com.chenpp.common.bigdata.hadoop;

import com.chenpp.common.security.LoginUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * @author April.Chen
 * @date 2024/2/18 15:14
 */

public class HdfsUtilTest {
    private HdfsConf hdfsConf;
    private FileSystem fileSystem;
    private HdfsKerberosConf kerberosConf;

    @Before
    public void init() throws IOException {
        kerberosConf = new HdfsKerberosConf();
        kerberosConf.setKerberosEnable(true);
        kerberosConf.setPrincipal("admin/admin@yuntu.com");
        kerberosConf.setKeytabPath("/Users/chenpp/bigdata/60/ark3/admin.keytab");
        kerberosConf.setKrb5Path("/Users/chenpp/bigdata/60/ark3/krb5.conf");
        kerberosConf.setNameNodePrincipal("nn/_HOST@yuntu.com");
        kerberosConf.setDataNodePrincipal("dn/_HOST@yuntu.com");


        hdfsConf = new HdfsConf();
        hdfsConf.setNameService("ns60");
        hdfsConf.setNameNode1("yuntu-qiye-e-010058012060.hz.td:8020");
        hdfsConf.setNameNode2("yuntu-qiye-e-010058012061.hz.td:8020");
        hdfsConf.setKerberosConf(kerberosConf);

        Configuration configuration = HdfsUtil.buildConfiguration(hdfsConf);
//        LoginUtil.setJaasFile(kerberosConf.getPrincipal(), kerberosConf.getKeytabPath());
        LoginUtil.login(kerberosConf.getPrincipal(), kerberosConf.getKeytabPath(), kerberosConf.getKrb5Path(), configuration);

        fileSystem = HdfsUtil.getInstance(hdfsConf);

    }

    @Test
    public void testGetInstance() {
        Assert.assertNotNull(fileSystem);
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
}
