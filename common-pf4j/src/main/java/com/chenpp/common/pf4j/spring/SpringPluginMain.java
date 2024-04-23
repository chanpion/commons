package com.chenpp.common.pf4j.spring;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author April.Chen
 * @date 2024/4/10 10:47
 */
public class SpringPluginMain {


    public static void main(String[] args) throws BeansException {
        // 启动PF4J-SPRING
        printLogo();

        // 加载自定义的配置类，jar包加载控制器
        // 这一步会先全局扫描插件，没有找到插件的话，就会找可能的extensions
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);

        // retrieves automatically the extensions for the Greeting.class extension point
        // 自动检索Greet.class扩展点的扩展名
        Greetings greetings = applicationContext.getBean(Greetings.class);
        greetings.printGreetings();

//        // stop plugins
//        PluginManager pluginManager = applicationContext.getBean(PluginManager.class);
//        // retrieves manually the extensions for the Greeting.class extension point
//        List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
//        System.out.println("greetings.size() = " + greetings.size());
//        pluginManager.stopPlugins();
    }

    private static void printLogo() {
        System.out.println(StringUtils.repeat("#", 40));
        System.out.println(StringUtils.center("PF4J-SPRING 已启动", 40));
        System.out.println(StringUtils.repeat("#", 40));
    }
}
