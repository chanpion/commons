package com.chenpp.common.code.graph;

import java.util.LinkedList;

/**
 * 邻接表是一种基于链表的图表示方法，其中每个顶点都对应一个链表，链表中存储与该顶点相连的顶点。
 * 这种方法适用于稀疏图，因为它可以避免存储大量的 0。
 *
 * @author April.Chen
 * @date 2023/11/9 10:19 上午
 **/
public class AdjacencyListGraph {
    private int V;
    private LinkedList<Integer>[] adjList;

    public AdjacencyListGraph(int v) {
        V = v;
        adjList = new LinkedList[v];
        for (int i = 0; i < v; i++) {
            adjList[i] = new LinkedList<>();
        }
    }

    public void addEdge(int v, int w) {
        adjList[v].add(w);
        adjList[w].add(v);
    }

    public void printGraph() {
        for (int i = 0; i < V; i++) {
            System.out.print("Vertex " + i + ": ");
            for (int j = 0; j < adjList[i].size(); j++) {
                System.out.print(adjList[i].get(j) + " ");
            }
            System.out.println();
        }
    }


    public static void main(String[] args) {
        AdjacencyListGraph graph = new AdjacencyListGraph(2);
        graph.addEdge(0,1);
        graph.addEdge(1,1);
        graph.printGraph();
    }
}
