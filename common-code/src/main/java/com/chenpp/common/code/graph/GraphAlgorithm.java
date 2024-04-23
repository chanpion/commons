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

    /**
     * Dijkstra 算法是一种用于计算单源最短路径的算法。它的基本思想是从起始节点开始，依次遍历所有可达节点，并更新它们的距离值。
     * 在每次遍历过程中，选择当前距离值最小的节点，并更新它的邻居节点的距离值。这个过程一直持续到所有节点的距离值都已知或者无法再更新为止。
     * Dijkstra 算法的实现通常涉及到一个优先队列（如二叉堆或斐波那契堆）来存储未知节点的距离值。
     * 在每次遍历过程中，我们从优先队列中选择距离值最小的节点，并更新它的邻居节点的距离值。
     * 如果某个邻居节点的距离值比当前节点的距离值更小，则更新它的距离值并将其加入优先队列中。
     *
     * @param graph 二维数组 graph
     * @param start 起始节点 start
     * @return  dist[i] 表示从起始节点到节点 i 的最短路径长度。
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
                if (graph[u][v] != 0 && dist[u] != Integer.MAX_VALUE && dist[u] + graph[u][v] < dist[v]) {
                    dist[v] = dist[u] + graph[u][v];
                    pq.offer(v);
                }
            }
        }

        return dist;
    }

    public static int[][][] floyd(int[][] graph) {
        int n = graph.length;
        int[][][] dist = new int[n][n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    if (graph[i][j] != 0 && graph[j][k] != 0) {
                        dist[i][j][k] = Math.min(dist[i][j][k], graph[i][j] + graph[j][k]);
                    }
                }
            }
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (graph[i][j] != 0 && graph[j][k] != 0) {
                        dist[i][j][k] = Math.min(dist[i][j][k], dist[i][j][k] + graph[k][j]);
                    }
                }
            }
        }

        return dist;
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

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.offer(new int[]{start, 0});

        while (!pq.isEmpty()) {
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
}
