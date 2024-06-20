package com.chenpp.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author April.Chen
 * @date 2024/6/20 09:34
 */
public class StringUtil {

    public static void main(String[] args) {
        // 纯数字："[0-9]+"
        // 包含数字和字母："[0-9a-zA-Z]+"
        // 包含数字：".*[0-9].*"
        // 纯字母："[a-zA-Z]+"
        // 包含字母：".*[a-zA-Z].*"
        // 中文："[\\u4e00-\\u9fa5]"
//        String text = "Hello, world! This is a test string with 123 numbers.";
        String text = "xyz-001测试123";
//        String regex = "\\w+";
        String regex = "[A-Za-z0-9\\-]+";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }
}
