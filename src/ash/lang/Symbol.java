package ash.lang;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Symbol implements Serializable {
	private static final long serialVersionUID = 6668290329248269533L;
	public final String name;

	private static final Map<String, Symbol> CACHE = new HashMap<>();
	
	public static Symbol create(String name) {
		Symbol symbol = CACHE.get(name);
		if (symbol == null) {
			symbol = new Symbol(name);
			CACHE.put(name, symbol);
		}
		return symbol;
	}
	
	private Symbol(String symbolName) {
		name = symbolName;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime + ((name == null) ? 0 : name.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		Symbol other = (Symbol) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
