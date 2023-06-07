package com.chenpp.common.pf4j;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * @author April.Chen
 * @date 2023/6/7 4:23 下午
 **/
@Configuration
public class SpringConfiguration {

    @Bean
    public SpringPluginManager pluginManager() {
        return new SpringPluginManager();
    }

    @Bean
    @DependsOn("pluginManager")
    public Greetings greetings() {
        return new Greetings();
    }

}
