package ash.lang;

import ash.compiler.Compiler;
import ash.vm.Closure;
import ash.vm.VM;
import ash.vm.VMFrame;
import bruce.common.utils.CommonUtils;

public final class LazyNode extends PersistentList {
	private static final long serialVersionUID = -2645887899648828103L;
	private Closure func;
	private PersistentList seq;
	
	public LazyNode(Closure tail) {
		func = tail;
	}

	private PersistentList valid() {
		if (seq == null) {
			seq = callFunc(func);
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

	private static PersistentList callFunc(Closure func) {
		return (PersistentList) new VM().runInMain(Compiler.astsToInsts(new Node(new Node(func))));
	}

	@Override
	public String toString() {
		return VMFrame.debugging ? CommonUtils.buildString('(', head(), " ...)") : super.toString();
	}
}
