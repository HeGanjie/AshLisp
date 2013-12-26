package ash.lang;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

// TODO : make more efficient
public class PersistentMap<K, V> implements Serializable {
	private static final long serialVersionUID = -434275790068739365L;
	private final Map<K, V> map;
	
	public PersistentMap() {
		map = new HashMap<>();
	}
	
	public PersistentMap(Map<K, V> initMap) {
		map = initMap;
	}

	public PersistentMap(K key, V val) {
		this();
		map.put(key, val);
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
		final int prime = 31;
		return prime + ((map == null) ? 0 : map.hashCode());
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
		return map.toString();
	}
	
}
