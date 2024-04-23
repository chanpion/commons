package com.chenpp.common.code.graph.algorithm;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * @author April.Chen
 * @date 2024/4/23 15:55
 */
public class AdjacentMatrix {
    static int[][] graph;
    static int[] dist;
    static int[] path = new int[7];
    static boolean[] isVisited = new boolean[7];

    public static void init() {
        graph = new int[][]{
                {0, 1, 4, 3, 0, 0, 0},
                {1, 0, 3, 0, 0, 0, 0},
                {4, 3, 0, 2, 1, 5, 0},
                {3, 0, 2, 0, 2, 0, 0},
                {0, 0, 1, 2, 0, 0, 0},
                {0, 0, 5, 0, 0, 0, 2},
                {0, 0, 0, 0, 0, 2, 0}
        };
        dist = new int[graph.length];
        Arrays.fill(dist, Integer.MAX_VALUE);
    }

    public static void dijkstra() {
        while (!isOver()) {
            int i = findMin();
            //设置为已遍历状态
            isVisited[i] = true;
            //遍历该节点邻接节点
            for (int j = 0; j < graph[i].length; j++) {
                if (graph[i][j] != 0 && !isVisited[j]) {
                    //更新dist、path
                    flashDistAndPath(i, j);
                }
            }
        }
        System.out.println(Arrays.toString(dist));
    }

    public static int findMin() {
        int min = Integer.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < dist.length; i++) {
            if (min > dist[i] && dist[i] >= 0 && !isVisited[i]) {
                min = dist[i];
                index = i;
            }
        }
        return index;
    }

    /**
     * 之前的dist值一定是之前该节点到根节点的最短路径开销
     */
    public static void flashDistAndPath(int i, int j) {
        if (!isVisited[j] && graph[i][j] >= 0) {
            if (graph[i][j] + dist[i] < dist[j]) {
                dist[j] = graph[i][j] + dist[i];
                path[j] = j;
            }
        }
    }

    public static boolean isOver() {
        for (boolean visited : isVisited) {
            if (!visited) {
                return false;
            }
        }
        return true;
    }


    public static int[] dijkstra(int[][] graph, int start) {
        int n = graph.length;
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[start] = 0;

        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingInt(i -> dist[i]));
        pq.offer(start);

        while (!pq.isEmpty()) {
            int u = pq.poll();
            for (int v = 0; v < n; v++) {
                if (graph[u][v] != -1 && dist[u] != Integer.MAX_VALUE && dist[u] + graph[u][v] < dist[v]) {
                    dist[v] = dist[u] + graph[u][v];
                    pq.offer(v);
                }
            }
        }

        return dist;
    }

    public static void floyd(int[][] graph) {
        int n = graph.length;
        int[][] dist = new int[n][n];

        // 初始化距离矩阵
        for (int i = 0; i < n; i++) {
            dist[i] = Arrays.copyOf(graph[i], n);
        }

        //循环更新矩阵的值
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    // 如果节点i到节点j通过节点k的路径比已知路径更短，则更新dist[i][j]
                    if (dist[i][k] != 0 && dist[k][j] != 0 &&
                            dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }

        //打印
        System.out.print("floyd: \n");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.printf("%5d  ", dist[i][j]);
            }
            System.out.print("\n");
        }
    }

    public static void floyd2(int[][] graph) {
        int n = graph.length;
        //初始化距离矩阵 distance
        int[][] distance = new int[n][n];

        for (int i = 0; i < n; i++) {
            distance[i] = Arrays.copyOf(graph[i], n);
        }
        //初始化路径
        int[][] path = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                path[i][j] = j;
            }
        }
        //开始 Floyd 算法
        //每个点为中转
        for (int i = 0; i < n; i++) {
            //所有入度
            for (int j = 0; j < n; j++) {
                //所有出度
                for (int k = 0; k < graph[j].length; k++) {
                    //以每个点为「中转」，刷新所有出度和入度之间的距离
                    //例如 AB + BC < AC 就刷新距离
                    if (distance[j][i] != -1 && distance[i][k] != -1) {
                        int newDistance = distance[j][i] + distance[i][k];
                        if (newDistance < distance[j][k] || distance[j][k] == -1) {
                            //刷新距离
                            distance[j][k] = newDistance;
                            //刷新路径
                            path[j][k] = i;
                        }
                    }
                }
            }
        }

        System.out.println("====distance====");
        for (int[] ints : distance) {
            for (int anInt : ints) {
                System.out.print(anInt + " ");
            }
            System.out.println();
        }

        System.out.println("====path====");
        for (int[] ints : path) {
            for (int anInt : ints) {
                System.out.print(anInt + " ");
            }
            System.out.println();
        }
    }


    public static void testFloyd() {
        int[][] graph = new int[][]{
                {0, 2, -1, 6}
                , {2, 0, 3, 2}
                , {-1, 3, 0, 2}
                , {6, 2, 2, 0}};
        System.out.println("====graph====");
        for (int[] ints : graph) {
            for (int anInt : ints) {
                System.out.print(anInt + " ");
            }
            System.out.println();
        }
        floyd2(graph);
    }

    public static void main(String[] args) {
//        testFloyd();
        init();
        isVisited[0] = true;
        dist[1] = 1;
        dist[2] = 4;
        dist[3] = 3;

        path[1] = 0;
        path[2] = 0;
        path[3] = 0;

        graph = new int[][]{
                {0, 2, -1, 6}
                , {2, 0, 3, 2}
                , {-1, 3, 0, 2}
                , {6, 2, 2, 0}};

        dijkstra();
//        floyd(graph);
    }
}
