package com.chenpp.common.bigdata.app.service;

import com.chenpp.common.bigdata.hive.HiveTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author April.Chen
 * @date 2024/7/4 15:06
 */
@Slf4j
@Service
public class HiveService implements ApplicationRunner {

    @Resource
    private HiveTemplate hiveTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> databases = hiveTemplate.showDatabases();
        log.info("databases: {}", databases);
    }
}
