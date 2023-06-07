package com.chenpp.common.pf4j;

import org.pf4j.Extension;

/**
 * @author April.Chen
 * @date 2023/6/7 4:15 下午
 **/
@Extension
public class WelcomeGreeting implements Greeting {

    public String getGreeting() {
        return "Welcome";
    }

}