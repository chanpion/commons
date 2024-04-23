package com.chenpp.common.code.graph;

/**
 * @author April.Chen
 * @date 2023/11/8 7:45 下午
 **/
public class Graph {

    static class Vertex<T> {
        T val;
        Vertex<T>[] adj;
    }
}
