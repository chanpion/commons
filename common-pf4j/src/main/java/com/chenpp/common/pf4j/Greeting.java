package com.chenpp.common.pf4j;

import org.pf4j.ExtensionPoint;

/**
 * @author April.Chen
 * @date 2023/6/7 4:15 下午
 **/
public interface Greeting extends ExtensionPoint {

    String getGreeting();

}