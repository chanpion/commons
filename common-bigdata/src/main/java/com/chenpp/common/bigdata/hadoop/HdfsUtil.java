package com.chenpp.common.bigdata.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author April.Chen
 * @date 2023/11/8 1:53 下午
 **/
public class HdfsUtil {
    private static Logger log = LoggerFactory.getLogger(HdfsUtil.class);

    public static final String HDFS_PREFIX = "hdfs://";
    private final static long KB = 1024L;
    public final static long MB = KB * KB;
    public final static long GB = KB * MB;
    private final static long TB = KB * GB;
    public static final String DFS_NAME_NODE_RPC_ADDRESS = "dfs.namenode.rpc-address.";

    /**
     * get FileSystem
     *
     * @param url  hdfs://192.168.0.106:8020
     * @param user root
     * @return FileSystem
     */
    public static FileSystem getInstance(String url, String user) {
        Configuration configuration = new Configuration();
        try {
            return FileSystem.get(new URI(url), configuration, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Configuration buildConfiguration(HdfsConf hdfsConf) {
        Configuration configuration = new Configuration(false);
        String nameService = hdfsConf.getNameService();

        String nn1 = hdfsConf.getNameNode1();
        String nn2 = hdfsConf.getNameNode2();
        String hdfsUrl = HDFS_PREFIX + nameService;
        if (nameService.startsWith(HDFS_PREFIX)) {
            hdfsUrl = nameService;
        }
        configuration.set("fs.defaultFS", hdfsUrl);
        configuration.set("dfs.nameservices", nameService);
        configuration.set("dfs.ha.namenodes." + nameService, "nn1,nn2");
        configuration.set(DFS_NAME_NODE_RPC_ADDRESS + nameService + ".nn1", nn1);
        configuration.set(DFS_NAME_NODE_RPC_ADDRESS + nameService + ".nn2", nn2);
        configuration.set("fs.hdfs.impl.disable.cache", "true");
        configuration.set("dfs.client.failover.proxy.provider." + nameService,
                "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        configuration.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        configuration.set("dfs.client.use.datanode.hostname", "true");
        if (hdfsConf.getKerberosConf() != null) {
            //需要增加hadoop开启了安全的配置
            configuration.setBoolean("hadoop.security.authorization", true);
            configuration.set("hadoop.security.authentication", "kerberos");
            //设置namenode的principal
            configuration.set("dfs.namenode.kerberos.principal", hdfsConf.getKerberosConf().getNameNodePrincipal());
            //设置datanode的principal值为“hdfs/_HOST@YOU-REALM.COM”
            configuration.set("dfs.datanode.kerberos.principal", hdfsConf.getKerberosConf().getDataNodePrincipal());
        }

        return configuration;
    }

    public static FileSystem getInstance(HdfsConf hdfsConf) {
        Configuration configuration = buildConfiguration(hdfsConf);
        try {
            return FileSystem.get(configuration);
        } catch (Exception e) {
            log.error("get hdfs connection error", e);
        }
        return null;
    }


    public static List<FileStatus> listFiles(FileSystem fs, String path) {
        try {
            FileStatus[] fileStatus = fs.listStatus(new Path(path));
            if (fileStatus == null || fileStatus.length == 0) {
                return null;
            }
            return Arrays.stream(fileStatus).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("list hdfs files error", e);
        }
        return null;
    }

    public static void upload(FileSystem fs, String sourceFile, String hdfsPath) throws IOException {
        fs.copyFromLocalFile(new Path(sourceFile), new Path(hdfsPath));
    }

    public static void uploadWithProcess(FileSystem fs, String sourceFile, String hdfsPath) throws IOException {
        FileInputStream inputStream = new FileInputStream(sourceFile);
        final float fileSize = inputStream.available();
        //显示上传进度
        FSDataOutputStream out = fs.create(new Path(hdfsPath),
                new Progressable() {
                    long fileCount = 0;

                    @Override
                    public void progress() {
                        fileCount++;
                        // progress 方法每上传大约 64KB 的数据后就会被调用一次
                        System.out.println("上传进度：" + (fileCount * 64 * 1024 / fileSize) + " %");
                    }
                });


        IOUtils.copyBytes(inputStream, out, 2048, true);
    }

    public static void download(FileSystem fs, String hdfsPath, String localPath) throws IOException {
        InputStream in = fs.open(new Path(hdfsPath));
        OutputStream out = new FileOutputStream(localPath);
        IOUtils.copyBytes(in, out, 4096, true);
        // or
        fs.copyToLocalFile(new Path(hdfsPath), new Path(localPath));
    }

    public static void mkdir(FileSystem fs, String dir) throws IOException {
        // 递归创建目录
        fs.mkdirs(new Path(dir), new FsPermission(FsAction.READ_WRITE, FsAction.READ, FsAction.READ));
    }

    public static boolean exists(FileSystem fs, String hdfsPath) throws IOException {
        return fs.exists(new Path(hdfsPath));
    }

    public static boolean rename(FileSystem fs, String oldPath, String newPath) throws IOException {
        return fs.rename(new Path(oldPath), new Path(newPath));
    }

    public static String readToString(FileSystem fs, String hdfsPath) {
        try {
            FSDataInputStream inputStream = fs.open(new Path(hdfsPath));
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String str = "";
            while ((str = reader.readLine()) != null) {
                builder.append(str).append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean delete(FileSystem fs, String hdfsPath) throws IOException {
        return fs.delete(new Path(hdfsPath), true);
    }
}
