package com.chenpp.common.code.tree;

/**
 * @author April.Chen
 * @date 2023/4/3 3:29 下午
 **/
public class Trie {

    public int[][] son;
    public int[] cnt;
    public int idx;

    public Trie() {
        son = new int[1001][26];
        cnt = new int[1001];
        idx = 0;
    }

    public void insert(String str) {
        //插入一个字符串
        int h = 0;
        for (int i = 0; i < str.length(); i++) {
            int u = str.charAt(i) - 'a';
            if (son[h][u] == 0) {
                son[h][u] = ++idx;
            }
            h = son[h][u];
        }

        cnt[h]++;
    }

    public int query(String str) {
        //查询str出现的次数
        int h = 0;
        for (int i = 0; i < str.length(); i++) {
            int u = str.charAt(i) - 'a';
            if (son[h][u] == 0) {
                return 0;
            }
            h = son[h][u];
        }
        return cnt[h];
    }

    public int queryByDfs(String str) {
        return dfs(str, 0, 0);
    }

    public int dfs(String str, int h, int i) {
        //dfs前缀树的第h个结点，字符串的第i个字符
        if (i == str.length()) {
            return cnt[h];
        }

        int u = str.charAt(i) - 'a';
        if (son[h][u] == 0) {
            return 0;
        }
        return dfs(str, son[h][u], i + 1);
    }

    public static void main(String[] args) {
        System.out.println();
        System.out.println("测试前缀树");
        Trie tree = new Trie();
        tree.insert("abcd");
        tree.insert("qwer");
        tree.insert("abc");
        tree.insert("abc");
        tree.insert("abdct");
        System.out.println("非递归查找:" + tree.query("abc"));
        System.out.println("递归查找:" + tree.queryByDfs("abc"));
        System.out.println("非递归查找:" + tree.query("abcde"));
        System.out.println("递归查找:" + tree.queryByDfs("abcde"));
    }
}
