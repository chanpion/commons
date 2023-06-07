package com.chenpp.common.pf4j;

import org.apache.commons.lang.StringUtils;
import org.pf4j.JarPluginManager;
import org.pf4j.PluginManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

/**
 * @author April.Chen
 * @date 2023/6/7 4:18 下午
 **/
public class Boot {
    public static void main1(String[] args) {

        // create the plugin manager
        // or "new ZipPluginManager() / new DefaultPluginManager()"
        PluginManager pluginManager = new JarPluginManager();
        // start and load all plugins of application
        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        // retrieve all extensions for "Greeting" extension point
        List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
        for (Greeting greeting : greetings) {
            System.out.println(">>> " + greeting.getGreeting());
        }

        // stop and unload all plugins
        pluginManager.stopPlugins();
        pluginManager.unloadPlugins();

    }


    public static void main(String[] args) throws BeansException, Throwable {
        // 启动PF4J-SPRING
        printLogo();

        // 加载自定义的配置类，jar包加载控制器
        // 这一步会先全局扫描插件，没有找到插件的话，就会找可能的extensions
        /*
         Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@cac736f
         Creating shared instance of singleton bean 'org.springframework.context.annotation.internalConfigurationAnnotationProcessor'
         Creating shared instance of singleton bean 'org.springframework.context.event.internalEventListenerProcessor'
         Creating shared instance of singleton bean 'org.springframework.context.event.internalEventListenerFactory'
         Creating shared instance of singleton bean 'org.springframework.context.annotation.internalAutowiredAnnotationProcessor'
         Creating shared instance of singleton bean 'org.springframework.context.annotation.internalCommonAnnotationProcessor'
         Creating shared instance of singleton bean 'springConfiguration'
         Creating shared instance of singleton bean 'pluginManager'
         INFO org.pf4j.DefaultPluginStatusProvider - Enabled plugins: []
         INFO org.pf4j.DefaultPluginStatusProvider - Disabled plugins: []
         INFO org.pf4j.DefaultPluginManager - PF4J version 3.5.0 in 'deployment' mode
         DEBUG org.pf4j.AbstractPluginManager - Lookup plugins in '[plugins]'
         WARN org.pf4j.AbstractPluginManager - No 'plugins' root
         INFO org.pf4j.AbstractPluginManager - No plugins
         DEBUG org.pf4j.LegacyExtensionFinder - Reading extensions storages from classpath
         DEBUG org.pf4j.LegacyExtensionFinder - Read '/Users/lihui/Documents/Java/pf4j-spring/pf4j-spring/demo/app/target/classes/META-INF/extensions.idx'
         DEBUG org.pf4j.LegacyExtensionFinder - Read '/Users/lihui/Documents/Java/pf4j-spring/pf4j-spring/pf4j-spring/target/classes/META-INF/extensions.idx'
         DEBUG org.pf4j.LegacyExtensionFinder - Read '/Users/lihui/Documents/Java/pf4j-spring/pf4j-spring/demo/api/target/classes/META-INF/extensions.idx'
         DEBUG org.pf4j.AbstractExtensionFinder - Found possible 1 extensions:
         DEBUG org.pf4j.AbstractExtensionFinder -    org.pf4j.demo.WhazzupGreeting
         DEBUG org.pf4j.LegacyExtensionFinder - Reading extensions storages from plugins
         DEBUG org.pf4j.spring.ExtensionsInjector - Register extension 'org.pf4j.demo.WhazzupGreeting' as bean
         DEBUG org.pf4j.spring.SpringExtensionFactory -   Extension class ' org.pf4j.demo.WhazzupGreeting' belongs to a non spring-plugin (or main application) 'system, but the used PF4J plugin-manager is a spring-plugin-manager. Therefore the extension class will be autowired by using the managers application contexts
         DEBUG org.pf4j.spring.SpringExtensionFactory - Instantiate extension class 'org.pf4j.demo.WhazzupGreeting' by using constructor autowiring.
         DEBUG org.pf4j.spring.SpringExtensionFactory - Completing autowiring of extension: org.pf4j.demo.WhazzupGreeting@363ee3a2
         DEBUG org.springframework.beans.factory.support.DefaultListableBeanFactory - Creating shared instance of singleton bean 'greetings'
         */
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);

        // retrieves automatically the extensions for the Greeting.class extension point
        // 自动检索Greet.class扩展点的扩展名
        Greetings greetings = applicationContext.getBean(Greetings.class);
        greetings.printGreetings();

        // stop plugins
        PluginManager pluginManager = applicationContext.getBean(PluginManager.class);
        /*
        // retrieves manually the extensions for the Greeting.class extension point
        List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
        System.out.println("greetings.size() = " + greetings.size());
        */
        pluginManager.stopPlugins();
    }

    private static void printLogo() {
        System.out.println(StringUtils.repeat("#", 40));
        System.out.println(StringUtils.center("PF4J-SPRING 已启动", 40));
        System.out.println(StringUtils.repeat("#", 40));
    }
}
