package ash.lang;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bruce.common.utils.CommonUtils;

public abstract class PersistentList implements Serializable, Iterable<PersistentList> {
	private static final long serialVersionUID = -4993574552940681092L;
	public abstract Object head();
	public abstract PersistentList rest();
	
	@Override
	public Iterator<PersistentList> iterator() {
		return new Iterator<PersistentList>() {
			PersistentList head = PersistentList.this;
			@Override
			public boolean hasNext() { return BasicType.NIL != head; }
			@Override
			public PersistentList next() {
				PersistentList curr = head;
				head = head.rest();
				return curr;
			}
			@Override
			public void remove() { throw new UnsupportedOperationException(); }
		};
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> toList(Class<T> c) {
		List<T> arrayList = new ArrayList<>();
		for (PersistentList n : this) {
			arrayList.add((T) n.head());
		}
		return arrayList;
	}
	
	private String innerToString() {
		StringBuilder sb = new StringBuilder();
		Object left = head();
		PersistentList next = rest();
		
		if (left instanceof Node)
			sb.append(left);
		else if (left != null)
			sb.append(BasicType.asString(left));
		
		if (BasicType.NIL != next && BasicType.NIL != this) {
			sb.append(' ');
			sb.append(next.innerToString());
		}
		return sb.toString();
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
}
