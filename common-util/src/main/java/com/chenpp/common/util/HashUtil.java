package com.chenpp.common.util;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

/**
 * hash工具，可用于短链接
 *
 * @author April.Chen
 * @date 2024/3/28 14:53
 */
public class HashUtil {
    private static char[] CHARS = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };
    private static final int SIZE = CHARS.length;

    /**
     * 将10进制数字转换为62进制
     *
     * @param num 10进制数字
     * @return 62进制字符串
     */
    public static String base62(int num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int i = num % SIZE;
            sb.append(CHARS[i]);
            num /= SIZE;
        }
        return sb.reverse().toString();
    }

    /**
     * MurmurHash
     */
    public static int hash(String str) {
        int hash = Hashing.murmur3_32().hashString(str, StandardCharsets.UTF_8).asInt();
        return Math.abs(hash);
    }

    public static String base62(String str) {
        return base62(hash(str));
    }
}
