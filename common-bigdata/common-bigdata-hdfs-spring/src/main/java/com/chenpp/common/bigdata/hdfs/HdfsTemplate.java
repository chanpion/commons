package com.chenpp.common.bigdata.hdfs;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author April.Chen
 * @date 2024/7/2 15:57
 */
@Slf4j
public class HdfsTemplate {

    private final HdfsProperties hdfsProperties;

    private FileSystem fileSystem;

    public HdfsTemplate(HdfsProperties hdfsProperties) {
        this.hdfsProperties = hdfsProperties;
        fileSystem = HdfsUtil.getFileSystem(this.hdfsProperties);
    }

    public List<FileStatus> listFiles(String path) {
        return HdfsUtil.listFiles(fileSystem, path);
    }


    public List<String> readFile(String path) {
        try (FSDataInputStream inputStream = fileSystem.open(new Path(path))) {
            return IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("read file failed", e);
            return null;
        }
    }
}
