package ash.vm;

import java.io.Serializable;
import java.util.List;

import ash.lang.ListUtils;
import ash.lang.Node;
import ash.lang.PersistentList;

public final class Closure implements Serializable {
	private static final long serialVersionUID = -1386281941599625466L;
	
	final List<Instruction> ins;
	final Scope env;
	int argsLimit = -2;
	final Node fnDefine;
	
	public Closure(List<Instruction> instructions, Scope environment, Node lambdaDefine) {
		ins = instructions;
		env = environment;
		fnDefine = lambdaDefine;
	}
	
	public int getArgsLimit() {
		if (argsLimit != -2) return argsLimit;
		if (ins.get(0).ins == InstructionSetEnum.cons_args.ordinal())
			return argsLimit = -1;
		return argsLimit = ListUtils.count(getArgsList());
	}

	@Override
	public String toString() {
		return fnDefine.toString();
	}

	public PersistentList getArgsList() {
		return (PersistentList) fnDefine.second();
	}
	
}
