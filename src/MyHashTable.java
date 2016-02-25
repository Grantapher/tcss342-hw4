import java.util.Iterator;

public class MyHashTable<K, V> implements Iterable<MyHashTable<K, V>.Entry<K, V>> {
    private Object[] myBuckets;
    private int[] myProbes;
    private final int myCapacity;
    private int myCount;
    private int myMaxProbes;

    @SuppressWarnings("unchecked")
    public MyHashTable(int capacity) {
        myBuckets = new Object[capacity];
        myProbes = new int[capacity];
        myCapacity = capacity;
        myCount = 0;
        myMaxProbes = 0;
    }

    void put(K key, V val) {
        int index = hash(key);
        Entry<K, V> entry = getEntry(index);
        if (null == entry) {
            entry = new Entry<>(key, val);
            myBuckets[index] = entry;
        } else {
            entry.setValue(val);
        }
    }

    V get(K key) {
        Entry<K, V> entry = getEntry(hash(key));
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

        sb.append("Histogram of Probes:\n[");
        int count = 0;
        for (int i = 0; i <= myMaxProbes; i++) {
            int num = myProbes[i];
            sb.append(num).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "]\n");

        sb.append("Fill Percentage: ").append((float) myCount / (float) myCapacity).append("%\n");
        sb.append("Max Linear Probe: ").append(myMaxProbes).append('\n');
        sb.append("Average Linear Probe: ").append(getAverageProbe()).append('\n');
        System.out.println(sb.toString());
    }

    private float getAverageProbe() {
        int total = 0;
        for (int i = 1; i < myMaxProbes; i++) {
            total += i * myProbes[i];
        }
        return (float) total / (float) myCount;
    }

    private int hash(K key) {
        int originalIndex = Math.abs(key.hashCode() * 31);
        int probeLength = 0;
        int index;
        for (index = originalIndex % myCapacity; myBuckets[index] != null; index = (index + 1) % myCapacity) {
            if (getEntry(index).matches(key)) return index;
            probeLength++;
        }
        myCount++;
        myProbes[probeLength]++;
        myMaxProbes = Math.max(myMaxProbes, probeLength);
        return index;
    }

    @SuppressWarnings("unchecked")
    private Entry<K, V> getEntry(int i) {
        return (Entry<K, V>) myBuckets[i];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (int i = 0; i < myCapacity; i++) {
            Entry<K, V> entry = getEntry(i);
            if (null == entry) continue;
            sb.append(entry).append(", ");
        }
        sb.replace(sb.length() - 3, sb.length(), " ]");
        return sb.toString();
    }

    @Override
    public Iterator<MyHashTable<K, V>.Entry<K, V>> iterator() {
        return new MapIterator();
    }

    private class MapIterator implements Iterator<MyHashTable<K, V>.Entry<K, V>> {
        private int myIndex = 0;

        @Override
        public boolean hasNext() {
            while (myIndex < myCapacity) {
                if (null != myBuckets[myIndex]) return true;
                myIndex++;
            }
            return false;
        }

        @Override
        public Entry<K, V> next() {
            return getEntry(myIndex++);
        }

        @Override
        public void remove() {
            throw new IllegalStateException("Cannot remove From MyHashTable Iterator.");
        }
    }

    public class Entry<A, B> {
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
            return "\"" + key.toString() + "\" -> " + value.toString();
        }
    }
}
