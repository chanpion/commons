package com.chenpp.common.code.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * 邻接矩阵是一种基于二维数组的图表示方法，其中每个顶点都对应一个二维数组的行和列。
 * 如果顶点 i 与顶点 j 之间有一条边，那么邻接矩阵中第 i 行第 j 列的值就为 1，否则为 0。
 * 这种方法简单易实现，但是对于稠密图来说，存储空间较大
 *
 * @author April.Chen
 * @date 2023/11/9 10:18 上午
 **/
public class AdjacentMatrixGraph {
    /**
     * 存放顶点
     */
    protected List<String> vertexes;
    /**
     * 邻接矩阵
     */
    private int[][] adj;
    private int V;

    /**
     * 顶点是否被访问
     */
    protected boolean[] isVisited;
    protected int numOfEdges;

    public AdjacentMatrixGraph(int n) {
        this.vertexes = new ArrayList<>(n);
        adj = new int[n][n];
    }

    public void addEdge(int v, int w) {
        adj[v][w] = 1;
        adj[w][v] = 1;
    }


    /**
     * 1. 获取节点个数
     */
    public int getNumOfVertex() {
        return vertexes.size();
    }

    /**
     * 2. 打印邻接矩阵
     */
    public void printGraph() {
        System.out.print(" ");
        for (String s : vertexes) {
            System.out.print("     " + s);
        }
        System.out.println();
        for (int r = 0; r < vertexes.size(); r++) {
            System.out.print(vertexes.get(r) + " ");
            for (int c = 0; c < vertexes.size(); c++) {
                System.out.print(String.format("%5d", adj[r][c]) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * 3. 获取边的数目
     */
    public int getNumOfEdges() {
        return numOfEdges;
    }

    /**
     * 4. 获取某条边的权值
     *
     * @param v1 from
     * @param v2 to
     * @return 边权重
     */
    public int getWeightOfEdges(int v1, int v2) {
        return adj[v1][v2];
    }

    /**
     * 5. 添加节点
     *
     * @param v
     */
    public void addVertex(String v) {
        vertexes.add(v);
    }

    /**
     * 6. 添加边（双向）
     *
     * @param v1     from
     * @param v2     to
     * @param weight 权重
     */
    public void addEdge(int v1, int v2, int weight) {
        adj[v1][v2] = weight;
        adj[v2][v1] = weight;
        numOfEdges++;
    }

    /**
     * 7.获取顶点索引对应的值
     */
    public String getValueByIndex(int i) {
        return vertexes.get(i);
    }

    /**
     * *****************************迪杰斯特拉算法*********************************************
     *
     * @param graph 图
     * @param v     起始点
     */
    public static void Dijkstra(AdjacentMatrixGraph graph, int v) {
        //顶点v到其他各顶点的距离
        int[] distance = new int[graph.getNumOfVertex()];
        //存储中间的遍历结果
        int[] visited = new int[graph.getNumOfVertex()];
        // 一、初始化
        init(v, distance, visited, graph);
        // 二、循环更新距离直到所有点都被遍历
        while (!ifEnd(visited)) {
            int minDistanceIndex = getMinDistanceIndex(distance, visited, v);
            update(minDistanceIndex, visited, distance, graph, v);
        }
        System.out.println(Arrays.toString(distance));
    }

    /**
     * 1.初始化（初始顶点需特殊处理）
     */
    public static void init(int beginIndex, int[] distance, int[] visited, AdjacentMatrixGraph graph) {
        for (int i = 0; i < distance.length; i++) {
            //先获得原始的距离数组
            if (graph.adj[beginIndex][i] != 0) {
                distance[i] = graph.adj[beginIndex][i];
            }
        }
        //距起始点的距离始终为0，后面更新时要额外注意避免修改
        distance[beginIndex] = 0;
        //起始点标记已访问
        visited[beginIndex] = 1;
        System.out.println("初始化...................");
        System.out.println("距离：");
        System.out.println(Arrays.toString(distance));
        System.out.println("已访问顶点：");
        System.out.println(Arrays.toString(visited));
        System.out.println();
    }

    /**
     * 2.返回distance中距离最短并未被访问的顶点索引
     */
    public static int getMinDistanceIndex(int[] distance, int[] visited, int v) {
        //假定最小值
        int minDis = Integer.MAX_VALUE;
        int minDisIndex = 0;
        for (int i = 0; i < distance.length; i++) {
            //要找未访问的点，同时不可以是出发点！
            if (distance[i] < minDis && i != v && visited[i] == 0) {
                minDis = distance[i];
                minDisIndex = i;
            }
        }
        return minDisIndex;
    }

    /**
     * 3.更新操作，每次都更新距离数组和已访问数组(重点难点在于更新距离)
     */
    public static void update(int index, int[] visited, int[] distance, AdjacentMatrixGraph graph, int v) {
        Map<String, String> modifiedVertex = new HashMap<>();
        int[] tempDis = new int[distance.length];
        //首先把index对应点与其他点的距离保存在临时变量中
        for (int i = 0; i < graph.getNumOfVertex(); i++) {
            if (graph.adj[index][i] != 0) {
                tempDis[i] = graph.adj[index][i];
            }
        }
        //修改距离，要加上从出发点到index顶点的距离。注意原始出发点不能动！
        for (int k = 0; k < graph.getNumOfVertex(); k++) {
            if (tempDis[k] != Integer.MAX_VALUE && k != v) {
                tempDis[k] += distance[index];
            }
        }
        //修改后的距离如果比原来的小，就更新distance，同样不能动原始出发点！（感觉这里像是动态规划的思想，需要动态调整到所有点的距离）
        for (int j = 0; j < graph.getNumOfVertex(); j++) {
            if (tempDis[j] < distance[j] && j != v) {
                modifiedVertex.put(graph.getValueByIndex(j), distance[j] + "->" + tempDis[j]);
                distance[j] = tempDis[j];
            }
        }
        //标记这个点已访问
        visited[index] = 1;
        //输出本次的更新结果
        System.out.println();
        System.out.print("距离： ");
        System.out.println(Arrays.toString(distance));
        System.out.print("更新距离： ");
        if (modifiedVertex.isEmpty()) {
            System.out.print("本次未更新");
        } else {
            for (Map.Entry<String, String> entry : modifiedVertex.entrySet()) {
                System.out.print(entry.getKey() + ":" + entry.getValue() + " ");
            }
        }
        System.out.println();
        System.out.print("已访问顶点： ");
        System.out.println(Arrays.toString(visited));
    }

    /**
     * 4.判断已访问数组是否满，满了就结束
     */
    public static Boolean ifEnd(int[] visited) {
        for (int i = 0; i < visited.length; i++) {
            if (visited[i] == 0) {
                return false;
            }
        }
        return true;
    }

    public static void testDijkstra() {
        AdjacentMatrixGraph graph = new AdjacentMatrixGraph(10);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");
        graph.addVertex("F");
        graph.addVertex("G");
        graph.addVertex("H");
        graph.addVertex("I");
        graph.addVertex("J");
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 2, 7);
        graph.addEdge(0, 6, 2);
        graph.addEdge(1, 6, 3);
        graph.addEdge(1, 3, 9);
        graph.addEdge(2, 4, 8);
        graph.addEdge(3, 5, 4);
        graph.addEdge(4, 5, 5);
        graph.addEdge(4, 6, 4);
        graph.addEdge(5, 6, 6);
        graph.addEdge(4, 7, 5);
        graph.addEdge(5, 7, 5);
        graph.addEdge(5, 8, 4);
        graph.addEdge(7, 8, 3);
        graph.addEdge(3, 9, 6);
        graph.addEdge(8, 9, 2);
        //权为0的边权都等于65536.
        for (int i = 0; i < graph.getNumOfVertex(); i++) {
            for (int j = 0; j < graph.getNumOfVertex(); j++) {
                if (graph.adj[i][j] == 0) {
                    graph.adj[i][j] = Integer.MAX_VALUE;
                }
            }
        }
        System.out.println("边的数量： " + graph.getNumOfEdges());
        System.out.println("顶点的数量： " + graph.getNumOfVertex());
        System.out.println("邻接矩阵：");
        graph.printGraph();
        AdjacentMatrixGraph.Dijkstra(graph, 6);
    }

    /**
     * *****************************弗洛伊德算法*********************************************
     */
    public static void Floyd(AdjacentMatrixGraph graph) {
        //记录各顶点间的距离
        int[][] distance = graph.adj;
        // mid-中间顶点  begin-起始顶点  end-终点顶点
        for (int mid = 0; mid < graph.getNumOfVertex(); mid++) {
            System.out.println("\n" + graph.getValueByIndex(mid) + "作为中间顶点...");
            for (int begin = 0; begin < graph.getNumOfVertex(); begin++) {
                for (int end = 0; end < graph.getNumOfVertex(); end++) {
                    //对于两个顶点A、B，以及他们中间的顶点C，
                    // 如果A->C->B 的距离比 A->B 的距离短，就更新A、B间的距离
                    int newDis = distance[begin][mid] + distance[mid][end];
                    if (distance[begin][end] > newDis && begin != end) {
                        System.out.println("修改距离：" + graph.getValueByIndex(begin) + "->" + graph.getValueByIndex(end)
                                + " " + distance[begin][end] + ", 修改为 :" + newDis);
                        distance[begin][end] = newDis;
                        //更新距离,对于无向图 Lij=Lji
                        distance[end][begin] = newDis;
                    }
                }
            }
        }
        //结果展示
        System.out.println(" " + "各个顶点间的最终最短距离：");
        for (int i = 0; i < graph.getNumOfVertex(); i++) {
            for (int j = 0; j < graph.getNumOfVertex(); j++) {
                if (distance[i][j] != 65536) {
                    System.out.print(" " + graph.getValueByIndex(i) + "->" + graph.getValueByIndex(j) + " " + distance[i][j] + ",");
                }
            }
            System.out.println("\n");
        }
    }

    public void testFloyd() {
        AdjacentMatrixGraph graph = new AdjacentMatrixGraph(10);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");
        graph.addVertex("F");
        graph.addVertex("G");
        graph.addVertex("H");
        graph.addVertex("I");
        graph.addVertex("J");
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 2, 7);
        graph.addEdge(0, 6, 2);
        graph.addEdge(1, 6, 3);
        graph.addEdge(1, 3, 9);
        graph.addEdge(2, 4, 8);
        graph.addEdge(3, 5, 4);
        graph.addEdge(4, 5, 5);
        graph.addEdge(4, 6, 4);
        graph.addEdge(5, 6, 6);
        graph.addEdge(4, 7, 5);
        graph.addEdge(5, 7, 5);
        graph.addEdge(5, 8, 4);
        graph.addEdge(7, 8, 3);
        graph.addEdge(3, 9, 6);
        graph.addEdge(8, 9, 2);
        //权为0的边权都等于65536.
        for (int i = 0; i < graph.getNumOfVertex(); i++) {
            for (int j = 0; j < graph.getNumOfVertex(); j++) {
                if (graph.adj[i][j] == 0) {
                    graph.adj[i][j] = 65536;
                }
            }
        }
        System.out.println("边的数量： " + graph.getNumOfEdges());
        System.out.println("顶点的数量： " + graph.getNumOfVertex());
        System.out.println("邻接矩阵：");
        graph.printGraph();
        AdjacentMatrixGraph.Floyd(graph);
    }

    public static void main(String[] args) {
        testDijkstra();
    }

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

}
