package com.chenpp.common.code.tree;

import java.util.HashMap;
import java.util.Map;

/**
 * @author April.Chen
 * @date 2023/11/8 7:05 下午
 **/
public class TrieTree {

    public void insert(TrieNode root, String word) {
        //遍历该字符串的字符数组
        for (char c : word.toCharArray()) {
            //如果该节点的下层不包含此字符，那么加入一个新节点进去
            if (!root.next.containsKey(c)) {
                root.next.put(c, new TrieNode());
            }
            //查找下一层节点
            root = root.next.get(c);
        }
        //遍历字符串完毕，最后的节点isEnd置为true，表示一个字符串的结束
        root.isEnd = true;
    }

    /**
     * 查找字符串
     *
     * @param word
     * @return
     */
    public boolean search(TrieNode root, String word) {
        TrieNode end = searchPrefix(root, word);
        return end != null && end.isEnd;
    }

    /**
     * 匹配前缀
     */
    public boolean startsWith(TrieNode root, String prefix) {
        return searchPrefix(root, prefix) != null;
    }

    private TrieNode searchPrefix(TrieNode root, String prefix) {
        //初始默认为根节点，根节点不包含任何字符
        TrieNode cur = root;
        //遍历该字符串的字符数组
        for (char c : prefix.toCharArray()) {
            //如果该节点的下层不包含此字符，那么直接返回null
            if (!cur.next.containsKey(c)) {
                return null;
            }
            //查找下一层节点
            cur = cur.next.get(c);
        }
        return cur;
    }

    public static class TrieNode {
        /**
         * 经过该节点的字符串的下层节点
         */
        Map<Character, TrieNode> next;

        /**
         * 该节点是否是一个字符串的结束
         */
        boolean isEnd;

        public TrieNode() {
            this.next = new HashMap<>();
            this.isEnd = false;
        }

        public Map<Character, TrieNode> getNext() {
            return next;
        }

        public void setNext(Map<Character, TrieNode> next) {
            this.next = next;
        }

        public boolean isEnd() {
            return isEnd;
        }

        public void setEnd(boolean end) {
            isEnd = end;
        }
    }
}
