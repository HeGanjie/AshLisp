package ash.lang;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bruce.common.utils.CommonUtils;

public abstract class PersistentList implements Serializable, Iterable<Object> {
	private static final long serialVersionUID = -4993574552940681092L;
	public abstract Object head();
	public abstract PersistentList rest();
	
	@Override
	public Iterator<Object> iterator() {
		return new Iterator<Object>() {
			PersistentList curr = PersistentList.this;
			@Override
			public boolean hasNext() { return !curr.isEndingNode(); }
			@Override
			public Object next() {
				Object val = curr.head();
				curr = curr.rest();
				return val;
			}
			@Override
			public void remove() { throw new UnsupportedOperationException(); }
		};
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> toList(Class<T> c) {
		List<T> arrayList = new ArrayList<>();
		for (Object n : this) {
			arrayList.add((T) n);
		}
		return arrayList;
	}
	
	public List<Object> toList() {
		return toList(Object.class);
	}
		
	String innerToString() {
		StringBuilder sb = new StringBuilder();
		Object left = head();
		PersistentList next = rest();
		
		if (left instanceof Node)
			sb.append(left);
		else if (left != null)
			sb.append(BasicType.asString(left));
		
		if (!(isEndingNode() || next.isEndingNode())) {
			sb.append(' ');
			sb.append(next.innerToString());
		}
		return sb.toString();
	}
	
	public boolean isEndingNode() {
		return rest() == null;
	}
	
	@Override
	public String toString() {
		return CommonUtils.buildString('(', innerToString(), ')');
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		Object left = head();
		PersistentList next = rest();
		int result = prime + ((left == null) ? 0 : left.hashCode());
		return prime * result + ((next == null) ? 0 : next.hashCode());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Node other = (Node) obj;
		
		Object left = head();
		if (left == null) {
			if (other.head() != null)
				return false;
		} else if (!left.equals(other.head()))
			return false;
		
		PersistentList next = rest();
		if (next == null) {
			if (other.rest() != null)
				return false;
		} else if (!next.equals(other.rest()))
			return false;
		return true;
	}
	
	public static PersistentList cast(Object val) {
		if (val instanceof String)
			return CharNode.create((String) val);
		else if (val instanceof PersistentList) {
			return (PersistentList) val;
		} else if (val instanceof Iterable<?>) {
			return LazyNode.create(((Iterable<?>) val).iterator());
		}
		throw new IllegalArgumentException(val.getClass() + " Should Be Iterable at least.");
	}
}
