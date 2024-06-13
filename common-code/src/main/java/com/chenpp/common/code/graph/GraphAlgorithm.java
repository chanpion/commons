package com.chenpp.common.code.graph;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Kruskal 算法是一种用于最小生成树的算法。它的基本思想是从所有边中选择最小权值的边，并将它们添加到最小生成树中。
 * 在每次选择边时，我们需要确保所选边不会形成回路，否则就需要忽略该边。
 *
 * @author April.Chen
 * @date 2023/11/8 5:00 下午
 **/
public class GraphAlgorithm {
    private static final int INF = -1;

    /**
     * Dijkstra 算法是一种用于计算单源最短路径的算法。它的基本思想是从起始节点开始，依次遍历所有可达节点，并更新它们的距离值。
     * 在每次遍历过程中，选择当前距离值最小的节点，并更新它的邻居节点的距离值。这个过程一直持续到所有节点的距离值都已知或者无法再更新为止。
     * Dijkstra 算法的实现通常涉及到一个优先队列（如二叉堆或斐波那契堆）来存储未知节点的距离值。
     * 在每次遍历过程中，我们从优先队列中选择距离值最小的节点，并更新它的邻居节点的距离值。
     * 如果某个邻居节点的距离值比当前节点的距离值更小，则更新它的距离值并将其加入优先队列中。
     *
     * @param graph 邻接矩阵二维数组 graph
     * @param start 起始节点 start
     * @return dist[i] 表示从起始节点到节点 i 的最短路径长度。
     */
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
                if (graph[u][v] != 0 && graph[u][v] != INF && dist[u] != Integer.MAX_VALUE && dist[u] + graph[u][v] < dist[v]) {
                    dist[v] = dist[u] + graph[u][v];
                    pq.offer(v);
                }
            }
        }

        return dist;
    }

    /**
     * 所有节点间的最短路径
     *
     * @param graph
     * @return
     */
    public static int[][] floyd(int[][] graph) {
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
                    if (dist[i][k] != -1 && dist[k][j] != -1 && dist[i][k] + dist[k][j] < dist[i][j] || dist[i][j] == -1) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }
        return dist;
    }

    /**
     * 所有节点间的最短路径，并输出路径
     *
     * @param graph
     * @return
     */
    public static void floydWithPath(int[][] graph) {
        //初始化距离矩阵 distance
        int n = graph.length;
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
                for (int k = 0; k < n; k++) {
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


    /**
     * Prim 算法是一种用于最小生成树的算法。它的基本思想是从一个节点开始，依次选择与该节点相邻的最小权值的边，并将它们添加到最小生成树中。
     * 在每次选择边时，我们需要确保所选边不会形成回路，否则就需要忽略该边
     *
     * @param graph
     * @param start
     * @return
     */
    public static int[][] prim(int[][] graph, int start) {
        int n = graph.length;
        int[][] mst = new int[n][n];
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[start] = 0;

        // a[0]: 下表，a[1]：边权值
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.offer(new int[]{start, 0});

        while (!pq.isEmpty()) {
            System.out.println("pq:" + pq);
            int[] u = pq.poll();
            int uIdx = u[0];
            int uDist = u[1];

            for (int v = 0; v < n; v++) {
                if (graph[uIdx][v] != 0 && dist[v] > graph[uIdx][v]) {
                    dist[v] = graph[uIdx][v];
                    pq.offer(new int[]{v, dist[v]});
                }
            }

            for (int v = 0; v < n; v++) {
                if (graph[uIdx][v] != 0 && mst[uIdx][v] == 0 && dist[v] < Integer.MAX_VALUE) {
                    mst[uIdx][v] = graph[uIdx][v];
                    mst[v][uIdx] = graph[uIdx][v];
                }
            }
        }

        return mst;
    }

    public static void main(String[] args) {
        int[][] graph = new int[][]{
                {0, 1, 4, 3, INF, INF, INF},
                {1, 0, 3, INF, INF, INF, INF},
                {4, 3, 0, 2, 1, 5, INF},
                {3, INF, 2, 0, 2, INF, INF},
                {INF, INF, 1, 2, 0, INF, INF},
                {INF, INF, 5, INF, INF, 0, 2},
                {INF, INF, INF, INF, INF, 2, 0}
        };
        graph = new int[][]{
                {0, 2, -1, 6}
                , {2, 0, 3, 2}
                , {-1, 3, 0, 2}
                , {6, 2, 2, 0}};
        printGraph(graph);

        int[] dist = dijkstra(graph, 0);
        System.out.println("Dijkstra:");
        System.out.println(Arrays.toString(dist));

        int[][] floyd = floyd(graph);
        System.out.println("Floyd:");
        for (int[] ints : floyd) {
            System.out.println(Arrays.toString(ints));
        }


        floydWithPath(graph);
        System.out.println("Prim:");
        int[][] r = prim(graph, 0);
        printGraph(r);
    }

    public static void printGraph(int[][] graph) {
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph[i].length; j++) {
                System.out.printf("%5d  ", graph[i][j]);
            }
            System.out.println();
        }
    }
}
