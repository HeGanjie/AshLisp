package ash.lang;

import ash.util.JavaUtils;
import ash.vm.Closure;
import ash.vm.VM;

import java.util.Iterator;

public final class LazyNode extends PersistentList {
	private static final long serialVersionUID = -2645887899648828103L;
	private Closure func;
	private PersistentList seq;
	
	public LazyNode(Closure tail) {
		func = tail;
	}

	private PersistentList valid() {
		if (seq == null) {
			seq = (PersistentList) func.applyTo(BasicType.NIL);
		}
		return seq;
	}
	
	@Override
	public Object head() {
		return valid().head();
	}

	@Override
	public PersistentList rest() {
		return valid().rest();
	}

	@Override
	public String toString() {
		return VM.debugging ? JavaUtils.buildString('(', head(), " ...)") : super.toString();
	}

	public static PersistentList create(Iterator<?> iter) {
		// (lazy-iterator iter)
		Closure lazyIterator = (Closure) VM.tempVar.get(Symbol.create("lazy-iterator"));
		return (PersistentList) lazyIterator.applyTo(new Node(iter));
	}
}
