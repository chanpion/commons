package com.chenpp.common.bigdata.app.service;

import com.chenpp.common.bigdata.hbase.HBaseTemplate;
import com.chenpp.common.bigdata.hbase.HbaseRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author April.Chen
 * @date 2024/7/5 13:45
 */
@Slf4j
@Service
public class HBaseService implements ApplicationRunner {

    @Resource
    private HBaseTemplate hBaseTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Set<HbaseRow> rows = hBaseTemplate.scan("cpp:cpp_test_2319_indicator_entity");
        log.info("rows: {}", rows);
    }
}
