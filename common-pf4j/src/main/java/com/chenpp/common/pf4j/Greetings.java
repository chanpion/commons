package com.chenpp.common.pf4j;

/**
 * @author April.Chen
 * @date 2023/6/7 4:25 下午
 **/
public class Greetings {

    @Autowired
    private List<Greeting> greetings;

    public void printGreetings() {
        System.out.println(String.format("Found %d extensions for extension point '%s'", greetings.size(), Greeting.class.getName()));
        for (Greeting greeting : greetings) {
            System.out.println(">>> " + greeting.getGreeting());
        }
    }

}