import java.util.ArrayList;

public class MyHashTable<K, V> {
    private Entry<K, V>[] myBuckets;
    private int[] myProbes;
    private final int myCapacity;
    private int myCount;
    private int myMaxProbes;

    @SuppressWarnings("unchecked")
    public MyHashTable(int capacity) {
        myBuckets = (Entry<K, V>[]) new Object[capacity];
        myProbes = new int[capacity];
        myCapacity = capacity;
        myCount = 0;
        myMaxProbes = 0;
    }

    void put(K key, V val) {
        int index = hash(key);
        Entry<K, V> entry = myBuckets[index];
        if (null == entry) {
            entry = new Entry<>(key, val);
            myBuckets[index] = entry;
        } else {
            entry.setValue(val);
        }
    }

    V get(K key) {
        Entry<K, V> entry = myBuckets[hash(key)];
        return null == entry ? null : entry.getValue();
    }

    boolean containsKey(K key) {
        return null != myBuckets[hash(key)];
    }

    void stats() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hash Table Stats\n");
        sb.append("================\n");
        sb.append("Number of Entries: ").append(myCount).append('\n');
        sb.append("Number of Buckets: ").append(myCapacity).append('\n');

        sb.append("Histogram of Probes: ");
        for (int n : myProbes) {
            sb.append(n).append(", ");
        }
        sb.replace(sb.length() - 3, sb.length(), "]\n");

        sb.append("Fill Percentage: ").append((float) myCount / (float) myCapacity).append("%\n");
        sb.append("Max Linear Probe: ").append(myMaxProbes).append('\n');
        sb.append("Average Linear Probe: ").append(getAverageProbe()).append('\n');
    }

    private float getAverageProbe() {
        int total = 0;
        for (int i = 1; i < myMaxProbes; i++) {
            total += i * myProbes[i];
        }
        return (float) total / (float) myCount;
    }

    private int hash(K key) {
        int originalIndex = key.hashCode(), index;
        for (index = originalIndex % myCapacity; myBuckets[index] != null; index = (index + 1) % myCapacity) {
            if (myBuckets[index].matches(key)) return index;
        }
        int probeLength = index - originalIndex;
        myCount++;
        myProbes[probeLength]++;
        myMaxProbes = Math.max(myMaxProbes, probeLength);
        return index;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        //TODO check null?
        for (Entry<K, V> entry : myBuckets) {
            sb.append(entry).append(", ");
        }
        sb.replace(sb.length() - 3, sb.length(), " ]");
        return sb.toString();
    }

    private class Entry<A, B> {
        private final A key;
        private B value;

        public Entry(A key, B value) {
            this.key = key;
            this.value = value;
        }

        public boolean matches(A otherKey) {
            return key.equals(otherKey);
        }

        public B getValue() {
            return value;
        }

        public void setValue(B value) {
            this.value = value;
        }

        public A getKey() {
            return key;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public String toString() {
            return key.toString() + " -> " + value.toString();
        }
    }
}
