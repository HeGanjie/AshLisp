package ash.lang;

import java.util.Collection;
import java.util.Iterator;

public interface PersistentCollection<E> {
	public Collection<E> getCollection();

	public PersistentCollection<E> conj(E e);

	public PersistentCollection<E> merge(PersistentCollection<? extends E> c);

	public boolean contains(E o);

	public boolean containsAll(PersistentCollection<? extends E> c);

	public boolean isEmpty();

	public Iterator<E> iterator();

	public PersistentCollection<E> disj(E o);

	public PersistentCollection<E> disjAll(PersistentCollection<? extends E> c);

	public PersistentCollection<E> retainAll(PersistentCollection<? extends E> c);

	public int size();

	public Object[] toArray();

	public <T> T[] toArray(T[] a);

}
