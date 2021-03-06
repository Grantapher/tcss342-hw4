import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class CodingTree {
    public String message;
    public MyHashTable<String, String> codes;
    public HuffmanTree huffmanTree;
    public MyHashTable<String, Integer> counts;
    public List<Byte> bits;

    public CodingTree(String message) {
        this.message = message;
        counts = countItems(message);
        huffmanTree = new HuffmanTree(counts);
        codes = huffmanTree.generateCodes();
        bits = convertToBits(message);
    }

    private List<Byte> convertToBits(String message) {
        StringBuilder sb = new StringBuilder();

        //taken from http://stackoverflow.com/a/2206432
        final String withDelimiter = "((?<=%1$s)|(?=%1$s))";
        final String delimiterPattern = String.format(withDelimiter, "[^0-9A-Za-z'-]");
        String[] items = message.split(delimiterPattern);

        for (String item : items) {
            String code = codes.get(item);
            sb.append(code);
        }
        String converted = sb.toString();

        //taken from http://stackoverflow.com/a/23664301
        String[] splits = converted.split("(?<=\\G.{8})");
        List<Byte> bytes = new ArrayList<>();
        for (String split : splits) {
            byte b = parseBinary(split);
            bytes.add(b);
        }

        int padding = 8 - converted.length() % 8;
        if (padding != 8) {
            int finalIndex = bytes.size() - 1;
            byte finalByte = bytes.get(finalIndex);
            finalByte <<= padding;
            bytes.set(finalIndex, finalByte);
        }

        return bytes;
    }

    private byte parseBinary(String s) {
        byte b = 0;
        for (int i = 0; i < s.length(); i++) {
            b <<= 1;
            if ('1' == s.charAt(i)) b += 1;
        }
        return b;
    }

    private String byteToBinary(byte bits) {
        byte b = bits;
        String str = "";
        for (int i = 0; i < 8; i++) {
            str += (b & 0b10000000) > 0 ? '1' : '0';
            b <<= 1;
        }
        return str;
    }

    private MyHashTable<String, Integer> countItems(String message) {
        MyHashTable<String, Integer> counts = new MyHashTable<>(0x8000);

        //taken from http://stackoverflow.com/a/2206432
        final String withDelimiter = "((?<=%1$s)|(?=%1$s))";
        final String delimiterPattern = String.format(withDelimiter, "[^0-9A-Za-z'-]");

        String[] items = message.split(delimiterPattern);
        for (String item : items) {
            Integer count = counts.get(item);
            if (count == null) {
                counts.put(item, 1);
            } else {
                counts.put(item, count + 1);
            }
        }

        return counts;
    }

    public String decode(List<Byte> bits, MyHashTable<String, String> codes) {
        HuffmanTree tree = new HuffmanTree(codes, true);
        StringBuilder sb = new StringBuilder();
        HuffmanTree.Traverser traverser = tree.getTraverser();
        for (byte b : bits) {
            for (int i = 0; i < 8; i++) {
                boolean bitSet = (b & 0b1 << 7 - i) > 0;
                traverser.traverse(bitSet);
                if (traverser.isLeaf()) {
                    String item = traverser.getString();
                    sb.append(item);
                    traverser.reset();
                }
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        //don't print out novels
        boolean isLong = message.length() > 100;

        StringBuilder sb = new StringBuilder();
        if (!isLong) sb.append(message).append('\n');
        sb.append(codes.toString()).append('\n');
        if (!isLong) {
            for (byte b : bits) {
                String bin = byteToBinary(b);
                sb.append(bin).append(' ');
            }
            sb.append('\n');
        }
        sb.append(counts.toString()).append('\n');
        if (!isLong) sb.append(huffmanTree).append('\n');
        return sb.toString();
    }

    private class HuffmanTree {
        private Node root;

        private HuffmanTree(MyHashTable<String, Integer> counts) {
            PriorityQueue<Node> pq = new PriorityQueue<>();
            for (MyHashTable<String, Integer>.Entry<String, Integer> entry : counts) {
                Node newNode = new Node(entry);
                pq.offer(newNode);
            }

            while (pq.size() > 1) {
                pq.offer(pq.poll().merge(pq.poll()));
            }

            root = pq.poll();
        }

        public HuffmanTree(MyHashTable<String, String> codes, boolean isCodes) {
            if (!isCodes)
                throw new IllegalStateException("Java Generic type erasure is dumb, change this to true or " +
                        "delete it for the other constructor.");

            root = new Node();
            for (MyHashTable<String, String>.Entry<String, String> entry : codes) {
                addCode(entry);
            }

        }

        private void addCode(MyHashTable<String, String>.Entry<String, String> entry) {
            Node current = root;
            String item = entry.getKey();
            String path = entry.getValue();

            for (int i = 0; i < path.length(); i++) {
                boolean isLeft = '0' == path.charAt(i);
                if (isLeft) {
                    if (null == current.left) current.left = new Node();
                    current = current.left;
                } else {
                    if (null == current.right) current.right = new Node();
                    current = current.right;
                }
            }

            current.data = item;
        }

        public MyHashTable<String, String> generateCodes() {
            MyHashTable<String, String> map = new MyHashTable<>(0x8000);
            generateCodesRecursive(map, root, "");
            return map;
        }

        private void generateCodesRecursive(MyHashTable<String, String> map, Node node, String code) {
            if (node.isLeaf()) {
                map.put(node.data, code);
            } else {
                generateCodesRecursive(map, node.left, code + "0");
                generateCodesRecursive(map, node.right, code + "1");
            }
        }

        public HuffmanTree.Traverser getTraverser() {
            return new Traverser();
        }

        private class Node implements Comparable<Node> {
            private Node left, right;
            private Integer count;
            private String data;

            private Node(String data, Integer count) {
                this.data = data;
                this.count = count;
            }

            private Node(MyHashTable<String, Integer>.Entry<String, Integer> entry) {
                this(entry.getKey(), entry.getValue());
            }

            public Node() {
                this(null, null);
            }

            public boolean isLeaf() {
                return null != data;
            }

            @Override
            public int compareTo(@SuppressWarnings("NullableProblems") Node o) {
                return Integer.compare(count, o.count);
            }

            /**
             * Combines the two nodes with a joining node with the counts of both side nodes added.
             * <p/>
             * "this" will be the left node and other will be the right node.
             *
             * @param other the right node
             * @return the joining node
             */
            public Node merge(Node other) {
                Node newNode = new Node(null, this.count + other.count);
                newNode.left = this;
                newNode.right = other;
                return newNode;
            }
        }

        private class Traverser {
            private Node current;

            public Traverser() {
                this.current = root;
            }

            public void traverse(boolean traverseRight) {
                if (traverseRight) current = current.right;
                else current = current.left;
            }

            public boolean isLeaf() {
                return current.isLeaf();
            }

            public String getString() {
                return current.data;
            }

            public void reset() {
                current = root;
            }
        }
    }
}
