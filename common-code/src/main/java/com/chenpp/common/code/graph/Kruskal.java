package com.chenpp.common.code.graph;

import java.util.Arrays;

/**
 * @author April.Chen
 * @date 2023/11/8 5:18 下午
 **/
public class Kruskal {
    public static int[][] kruskal(int[][] graph) {
        int n = graph.length;
        int[][] edges = new int[n * n - n][3];
        int idx = 0;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                edges[idx++] = new int[]{i, j, graph[i][j]};
            }
        }

        Arrays.sort(edges, (a, b) -> a[2] - b[2]);

        UnionFind uf = new UnionFind(n);
        int[][] mst = new int[n][n];
        int mstSize = 0;

        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            int w = edge[2];

            if (uf.find(u) != uf.find(v)) {
                uf.union(u, v);
                mst[u][v] = w;
                mst[v][u] = w;
                mstSize += w;
            }
        }

        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = mst[i][j];
            }
        }

        return result;
    }
}

class UnionFind {
    private int[] parent;
    private int[] rank;

    public UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }

    public void union(int x, int y) {
        int px = find(x);
        int py = find(y);

        if (rank[px] < rank[py]) {
            parent[px] = py;
        } else if (rank[px] > rank[py]) {
            parent[py] = px;
        } else {
            parent[py] = px;
            rank[px]++;
        }
    }
}
