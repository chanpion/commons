package com.chenpp.common.pf4j;

import com.chenpp.common.pf4j.spring.Greetings;
import com.chenpp.common.pf4j.spring.SpringConfiguration;
import org.apache.commons.lang.StringUtils;
import org.pf4j.JarPluginManager;
import org.pf4j.PluginManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.file.Paths;
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



    public static void loadPlugin() {
// jar插件管理器
        PluginManager pluginManager = new JarPluginManager();

        // 加载指定路径插件
        pluginManager.loadPlugin(Paths.get("plugins-0.0.1-SNAPSHOT.jar"));

        // 启动指定插件(也可以加载所有插件)
        pluginManager.startPlugin("welcome-plugin");

        // 执行插件
        List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
        for (Greeting greeting : greetings) {
            System.out.println(">>> " + greeting.getGreeting());
        }

        // 停止并卸载指定插件
        pluginManager.stopPlugin("welcome-plugin");
        pluginManager.unloadPlugin("welcome-plugin");
    }
}
