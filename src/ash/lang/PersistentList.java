package ash.lang;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class PersistentList implements Serializable, Iterable<PersistentList> {
	private static final long serialVersionUID = -4993574552940681092L;
	public abstract Serializable head();
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
}
