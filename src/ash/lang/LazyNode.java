package ash.lang;

import ash.compiler.Compiler;
import ash.vm.Closure;
import ash.vm.VM;
import ash.vm.VMFrame;
import bruce.common.utils.CommonUtils;

public final class LazyNode extends PersistentList {
	private static final long serialVersionUID = -2645887899648828103L;
	private final Object left;
	private final Closure func;
	
	private LazyNode(Object head, Closure tail) {
		left = head;
		func = tail;
	}

	public static PersistentList create(Object head, Object tail) {
		if (tail == BasicType.NIL)
			return new Node(head);
		return new LazyNode(head, (Closure) tail);
	}

	@Override
	public Object head() { return left; }

	@Override
	public PersistentList rest() {
		return (PersistentList) new VM().runInMain(Compiler.astsToInsts(new Node(new Node(func))));
	}

	@Override
	public String toString() {
		if (VMFrame.debugging)
			return CommonUtils.buildString('(', left, " ...)");
		return super.toString();
	}
}
