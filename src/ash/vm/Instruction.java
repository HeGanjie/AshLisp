package ash.vm;

import ash.util.JavaUtils;

import java.io.Serializable;

public final class Instruction implements Serializable {
	private static final long serialVersionUID = 4568764332998004451L;
	private static final InstructionSet[] INST_ARR = InstructionSet.values();
	
	final int ins;
	final Object args;
	
	public Instruction(InstructionSet instruction) {
		ins = instruction.ordinal();
		args = null;
	}
	
	public Instruction(InstructionSet instruction, Object instructionArgs) {
		ins = instruction.ordinal();
		args = instructionArgs;
	}

	@Override
	public String toString() {
		if (args != null)
			return JavaUtils.buildString(INST_ARR[ins], ' ', args);
		else
			return INST_ARR[ins].name();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + ((args == null) ? 0 : args.hashCode());
		return prime * result + ins;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Instruction other = (Instruction) obj;
		if (args == null) {
			if (other.args != null)
				return false;
		} else if (!args.equals(other.args))
			return false;
		if (ins != other.ins)
			return false;
		return true;
	}
}
