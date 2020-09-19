package com.example.huffmancoding;

public class Node implements Comparable<Node>{

        int uid, weight;
        char ch;
        Node  left, right;


        public Node(char ch, int weight, Node left, Node right) {
            uid = ++ HuffmanManager.uid;
            this.weight = weight;
            this.ch = ch;
            this.left = left;
            this.right = right;
        }

        @Override
        public int compareTo(Node o1) {
            return this.weight - o1.weight;
        }

}
