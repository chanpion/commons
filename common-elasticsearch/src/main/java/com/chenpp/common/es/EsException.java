package com.chenpp.common.es;

/**
 * @author April.Chen
 * @date 2024/4/18 16:52
 */
public class EsException extends RuntimeException {
    public EsException(String s, Exception e) {
        super(s, e);
    }
}
