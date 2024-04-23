package com.chenpp.common.code.graph;

/**
 * 邻接矩阵是一种基于二维数组的图表示方法，其中每个顶点都对应一个二维数组的行和列。
 * 如果顶点 i 与顶点 j 之间有一条边，那么邻接矩阵中第 i 行第 j 列的值就为 1，否则为 0。
 * 这种方法简单易实现，但是对于稠密图来说，存储空间较大
 *
 * @author April.Chen
 * @date 2023/11/9 10:18 上午
 **/
public class AdjacentMatrixGraph {
    private int[][] adjMatrix;
    private int V;

    public AdjacentMatrixGraph(int v) {
        V = v;
        adjMatrix = new int[v][v];
    }

    public void addEdge(int v, int w) {
        adjMatrix[v][w] = 1;
        adjMatrix[w][v] = 1;
    }

    public void printGraph() {
        for (int i = 0; i < V; i++) {
            for (int j = 0; j < V; j++) {
                System.out.print(adjMatrix[i][j] + " ");
            }
            System.out.println();
        }
    }
}
