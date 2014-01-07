package ash.vm;

import java.io.Serializable;
import java.util.List;

import ash.lang.ListUtils;
import ash.lang.Node;

public final class Closure implements Serializable {
	private static final long serialVersionUID = -1386281941599625466L;
	
	final List<Instruction> ins;
	final Scope env;
	int argsLimit = -2;
	public final Node argsList;
	
	public Closure(List<Instruction> instructions, Scope environment, Node argsSeq) {
		ins = instructions;
		env = environment;
		argsList = argsSeq;
	}
	
	public int getArgsLimit() {
		if (argsLimit != -2) return argsLimit;
		if (ins.get(0).ins == InstructionSetEnum.cons_args.ordinal())
			return argsLimit = -1;
		return argsLimit = ListUtils.count(argsList);
	}

}
