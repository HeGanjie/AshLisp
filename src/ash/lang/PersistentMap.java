package ash.lang;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bruce.common.utils.CommonUtils;

// TODO : make more efficient
public class PersistentMap<K, V> implements Serializable, Iterable<Entry<K, V>> {
	private static final long serialVersionUID = -434275790068739365L;
	private static final char HASH_MAP_START = '{', HASH_MAP_END = '}';
	private final Map<K, V> map;
	
	public PersistentMap() {
		this(new HashMap<K, V>());
	}
	
	public PersistentMap(Map<K, V> initMap) {
		map = initMap;
	}

	public PersistentMap(List<?> initVal) {
		this(initVal.toArray());
	}
	
	@SuppressWarnings("unchecked")
	public PersistentMap(Object... initVal) {
		this();
		for (int i = 0; i < initVal.length; i += 2) {
			map.put((K) initVal[i], (V) initVal[i + 1]);
		}
	}
	
	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	public boolean containsValue(V value) {
		return map.containsValue(value);
	}

	public PersistentCollection<Map.Entry<K, V>> entrySet() {
		return new PersistentSet<Entry<K, V>>(map.entrySet());
	}

	public V get(K key) {
		return map.get(key);
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public PersistentSet<K> keySet() {
		return new PersistentSet<K>(map.keySet());
	}

	public PersistentMap<K, V> assoc(K key, V value) {
		Map<K, V> newMap = new HashMap<>(map);
		newMap.put(key, value);
		return new PersistentMap<K, V>(newMap);
	}

	public PersistentMap<K, V> merge(PersistentMap<? extends K, ? extends V> m) {
		Map<K, V> newMap = new HashMap<K, V>(map);
		newMap.putAll(m.map);
		return new PersistentMap<K, V>(newMap);
	}

	public PersistentMap<K, V> dissoc(K key) {
		Map<K, V> newMap = new HashMap<>(map);
		newMap.remove(key);
		return new PersistentMap<K, V>(newMap);
	}

	public int size() {
		return map.size();
	}

	public PersistentVector<V> values() {
		return new PersistentVector<V>(map.values());
	}

	@Override
	public int hashCode() {
		return 31 + ((map == null) ? 0 : map.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PersistentMap<?, ?> other = (PersistentMap<?, ?>) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return CommonUtils.buildString(HASH_MAP_START,
				PersistentList.cast(this).innerToString(),
				HASH_MAP_END);
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		return new Iterator<Entry<K, V>>() {
			Iterator<Entry<K, V>> origin = map.entrySet().iterator();
			
			@Override
			public boolean hasNext() { return origin.hasNext(); }

			@Override
			public PersistentEntry<K, V> next() {
				return new PersistentEntry<>(origin.next());
			}

			@Override
			public void remove() { throw new UnsupportedOperationException(); }
		};
	}
	
	static class PersistentEntry<K, V> implements Entry<K, V> {
		private Entry<K, V> entry;
		
		public PersistentEntry(Entry<K, V> mapEntry) { entry = mapEntry; }

		@Override
		public K getKey() { return entry.getKey(); }

		@Override
		public V getValue() { return entry.getValue(); }

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int hashCode() {
			return 31 + ((entry == null) ? 0 : entry.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			PersistentEntry<?, ?> other = (PersistentEntry<?, ?>) obj;
			if (entry == null) {
				if (other.entry != null)
					return false;
			} else if (!entry.equals(other.entry))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return CommonUtils.buildString(BasicType.asString(getKey()),
					' ', BasicType.asString(getValue()));
		}
	}
}
