package com.chenpp.common.pf4j.spring;

import com.chenpp.common.pf4j.Greeting;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author April.Chen
 * @date 2023/6/7 4:25 下午
 **/
public class Greetings {

    @Resource
    private List<Greeting> greetingList;

    public void printGreetings() {
        System.out.println(String.format("Found %d extensions for extension point '%s'", greetingList.size(), Greeting.class.getName()));
        for (Greeting greeting : greetingList) {
            System.out.println(">>> " + greeting.getGreeting());
        }
    }

}