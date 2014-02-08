package ash.vm;

import java.io.Serializable;
import java.util.List;

import ash.compiler.Compiler;
import ash.lang.ListUtils;
import ash.lang.Node;
import ash.lang.PersistentList;

public class ClosureArgs implements Serializable {
	private static final long serialVersionUID = 6028752794396503066L;
	List<Instruction> ins;
	final Node fnBody;
	final PersistentList fnContext;
	int argsLimit = -2;
	
	public ClosureArgs(Node lambdaBody, PersistentList lambdaContext) {
		fnBody = lambdaBody;
		fnContext = lambdaContext;
	}

	public int getArgsLimit() { // unused
		if (argsLimit != -2) return argsLimit;
		if (getInsts().get(0).ins == InstructionSet.cons_args.ordinal())
			return argsLimit = -1;
		return argsLimit = ListUtils.count(getArgsList());
	}
	
	public PersistentList getArgsList() {
		return (PersistentList) fnBody.second();
	}
	
	List<Instruction> getInsts() {
		if (ins == null)
			ins = Compiler.compileLambda(fnBody, fnContext);
		return ins;
	}
}
