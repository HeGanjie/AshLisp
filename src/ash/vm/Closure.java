package ash.vm;

import java.io.Serializable;
import java.util.List;

public final class Closure implements Serializable {
	private static final long serialVersionUID = -1386281941599625466L;
	
	final List<Instruction> ins;
	final Scope env;
	
	public Closure(List<Instruction> instructions, Scope environment) {
		ins = instructions;
		env = environment;
	}
	
	
}
