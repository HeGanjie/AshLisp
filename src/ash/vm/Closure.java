package ash.vm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ash.lang.PersistentList;

public final class Closure implements Serializable {
	private static final long serialVersionUID = -1386281941599625466L;
	final ClosureArgs args;
	final Scope env;
	
	public Closure(ClosureArgs closureArgs, Scope environment) {
		args = closureArgs;
		env = environment;
	}

	public Object applyTo(Iterable<Object> args) {
		List<Instruction> ls = new ArrayList<>();
		for (Object arg : args) {
			ls.add(InstructionSet.ldc.create(arg));
		}
		int argsCount = ls.size();
		ls.add(InstructionSet.ldc.create(this));
		ls.add(InstructionSet.call.create(argsCount));
		ls.add(InstructionSet.halt.create());
		return new VM().runFrame(new VMFrame(ls, null));
	}

	public List<Instruction> getInsts() {
		return args.getInsts();
	}

	public PersistentList getArgsList() {
		return args.getArgsList();
	}

	@Override
	public String toString() {
		return args.fnBody.toString();
	}
}
