package com.example.huffmancoding;

import java.util.HashMap;
import java.util.PriorityQueue;

public class HuffmanManager {

    public HashMap<Character, Integer> frequencyMap;
    public HashMap<Character, String> codeMap;
    public HashMap<String, Character> codeForDecoding;
    public PriorityQueue<Node> minQueue;

    private String string;
    public Node root;
    static int uid = 0;


    public HuffmanManager(String string) {

        this.uid = 0;
        this.string = string;

        frequencyMap = new HashMap<>();
        codeMap = new HashMap<>();
        codeForDecoding = new HashMap<>();
        minQueue = new PriorityQueue<>();

    }

    public void generateCode(Node node, String code) {

        if(node == null) {
            return;
        }

        if (node.left == null && node.right == null ) {

            codeMap.put(node.ch, code);
            codeForDecoding.put(code, node.ch);

        } else {
            generateCode(node.left, code + '0');
            generateCode(node.right, code + '1');
        }

    }

    public void constructHuffmanTree() {

        createHeap();
        Node left, right;
        while (!minQueue.isEmpty()) {

            left = minQueue.poll();

            if (minQueue.peek() != null) {
                right = minQueue.poll();
                root = new Node('\0', left.weight + right.weight, left, right);

            } else {
                root = new Node('\0', left.weight, left, null);
            }

            if (minQueue.peek() != null) {
                minQueue.add(root);
            }

        }
    }

    public void createHeap() {

        for (char ch : frequencyMap.keySet()) {
            Node node = new Node(ch, frequencyMap.get(ch), null, null);
            minQueue.add(node);
        }

    }

    public void createFreqMap() {

        for(char ch : string.toCharArray()){

            if(frequencyMap.containsKey(ch))
                frequencyMap.put(ch , frequencyMap.get(ch) + 1);
            else
                frequencyMap.put(ch , 1);

        }
    }

    public String encodeFinally() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < string.length(); i++) {

            char ch = string.charAt(i);
            sb.append(codeMap.get(ch));

        }

        return sb.toString();
    }

}
