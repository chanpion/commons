package com.chenpp.common.bigdata.app.service;

import com.chenpp.common.bigdata.hdfs.HdfsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileStatus;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author April.Chen
 * @date 2024/7/2 18:04
 */
@Slf4j
@Service
public class HdfsService implements ApplicationRunner {

    @Resource
    private HdfsTemplate hdfsTemplate;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<FileStatus> files = hdfsTemplate.listFiles("/tmp");
        log.info("files: {}", files);
        files.forEach(f -> log.info("hdfs files: {}", f));

//        List<String> lines = hdfsTemplate.readFile("hdfs://ns60/tmp/risk_data_new5922.csv");
//        lines.forEach(l -> log.info("{}", l));
    }
}
