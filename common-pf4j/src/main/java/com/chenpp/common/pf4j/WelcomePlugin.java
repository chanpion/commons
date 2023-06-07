package com.chenpp.common.pf4j;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * @author April.Chen
 * @date 2023/6/7 4:16 下午
 **/
public class WelcomePlugin extends Plugin {

    public WelcomePlugin(PluginWrapper wrapper) {
        super(wrapper);

        // you can use "wrapper" to have access to the plugin context (plugin manager, descriptor, ...)
    }

    @Override
    public void start() {
        System.out.println("WelcomePlugin.start()");
    }

    @Override
    public void stop() {
        System.out.println("WelcomePlugin.stop()");
    }

    @Override
    public void delete() {
        System.out.println("WelcomePlugin.delete()");
    }

}