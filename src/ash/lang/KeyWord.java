package ash.lang;

import java.io.Serializable;
import java.util.Map;
import java.util.WeakHashMap;

public final class KeyWord implements Serializable {
	private static final long serialVersionUID = -5276674770932351129L;
	private final String name;

	private static final Map<String, KeyWord> CACHE = new WeakHashMap<>();
	
	public static KeyWord create(String name) {
		KeyWord keyWord = CACHE.get(name);
		if (keyWord == null) {
			keyWord = new KeyWord(name);
			CACHE.put(name, keyWord);
		}
		return keyWord;
	}
	
	private KeyWord(String keyWord) {
		name = keyWord;
	}
	
	public String name() { return name.substring(1); }
	
	@Override
	public String toString() { return name; }

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime + ((name == null) ? 0 : name.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		KeyWord other = (KeyWord) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
