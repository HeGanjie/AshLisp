package ash.vm;

import java.io.Serializable;
import java.util.Arrays;

import bruce.common.utils.CommonUtils;

public final class Instruction implements Serializable {
	private static final long serialVersionUID = 4568764332998004451L;
	
	final InstructionSetEnum ins;
	final Serializable [] args;
	
	public Instruction(InstructionSetEnum instruction) {
		ins = instruction;
		args = null;
	}
	
	public Instruction(InstructionSetEnum instruction, Serializable... instructionArgs) {
		ins = instruction;
		args = instructionArgs;
	}

	@Override
	public String toString() {
		if (args != null)
			return CommonUtils.buildString(ins, ' ', CommonUtils.displayArray(args, " "));
		else
			return ins.name();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + Arrays.hashCode(args);
		return prime * result + (ins == null ? 0 : ins.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Instruction other = (Instruction) obj;
		if (!Arrays.equals(args, other.args)) return false;
		if (ins != other.ins) return false;
		return true;
	}

}
