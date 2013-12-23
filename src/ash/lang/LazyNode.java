package ash.lang;

import java.io.Serializable;

import bruce.common.utils.CommonUtils;

import ash.compiler.Compiler;
import ash.vm.Closure;
import ash.vm.VM;
import ash.vm.VMFrame;

public final class LazyNode extends PersistentList {
	private static final long serialVersionUID = -2645887899648828103L;
	private final Serializable left;
	private final Closure func;
	
	private LazyNode(Serializable head, Closure tail) {
		left = head;
		func = tail;
	}

	public static PersistentList create(Serializable head, Serializable tail) {
		if (tail == BasicType.NIL)
			return new Node(head);
		return new LazyNode(head, (Closure) tail);
	}

	@Override
	public Serializable head() { return left; }

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
