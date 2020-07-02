package skiplist;

import java.util.Random;

/**
 * @author rafa gao
 */


public class SkipList<E> {

    // 晋升的概率
    private static final double P = 0.25;
    private static final int FULL_LEVEL = 32;

    // 头节点
    private Node<E> head;
    // 尾节点
    private Node<E> tail;

    // 当前的最大高度
    private int maxLevel = 0;

    // 节点数量
    private int size = 0;

    public SkipList() {
        // 头节点不存储任何的数据
        Node[] headNodes = new Node[FULL_LEVEL];
        head = new Node<>(null, FULL_LEVEL,headNodes , Double.MIN_VALUE);
        tail  = new Node<>(head, FULL_LEVEL,null, Double.MAX_VALUE);
        for (int i = 0; i <FULL_LEVEL ; i++) {
            headNodes[i] = tail;
        }
    }

    // 增加节点
    public void add(double score) {
        int level = computeLevel();
        updateMaxLevel(level);
        Node<E> newNode = new Node<>(level, score);
        // 从最大层开始遍历
        doAdd(score,head,newNode,maxLevel-1 );
        this.size++;
    }

    /**
     * 是否已经把节点插入了
     * @param score
     * @param node
     * @param newNode
     * @param atLevel
     * @return
     */
    private int doAdd(double score, Node node, Node<E> newNode,int atLevel) {
        Node[] next = newNode.next;
        int level = newNode.level;
        if (node == tail||node.score > score) {
            if (atLevel < level) {
                next[atLevel] = node;
            }
            return atLevel;
        }
        Node[] curNext = node.next;
        for (int i = atLevel; i >=0 ; i--) {
            int temp = doAdd(score, curNext[i], newNode, i);
            if (temp == i) {
                curNext[temp] = newNode;
                if (temp == 0) {
                    newNode.prev = node;
                }
            }else {
                i = temp;
            }

        }
        return -1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node node = head.next[0];
        int i = 0;
        while (node!=tail) {
            sb.append("[").append(node.score).append(",").append(node.level).append("]--");
            i++;
            if (i == 10) {
                sb.append("\n");
                i = 0;
            }
            node = node.next[0];
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    private void updateMaxLevel(int newLevel) {
        // 更新最大的层数
        if (newLevel > maxLevel) {
            maxLevel = newLevel;
        }
    }

    private static class Node<E>{

        // 前一个节点
        private Node prev;
        // 等级
        private int level;
        // 指向下一组节点
        private Node[] next;
        // 数据
        private double score;

        public Node(Node prev, int level, Node[] next, double score) {
            this.prev = prev;
            this.level = level;
            this.next = next;
            this.score = score;
        }

        public Node(int level,double score) {
            this.level = level;
            next = new Node[level];
            this.score = score;
        }
    }

    public int size() {
        return size;
    }

    // 计算节点的level
    private int computeLevel() {
        int level = 1;
        while (level < FULL_LEVEL&&Math.random()<P) {
            level++;
        }
        return level;
    }
}
