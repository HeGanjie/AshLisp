package ash.vm;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import bruce.common.functional.Func1;
import bruce.common.functional.LambdaUtils;


public enum InstructionSetEnum {
	ldp, // 0
	ldv,
	ldc,
	quote,
	
	asn, // 4
	cons_args,
	closure,
	jmp,
	
	jz, // 8
	tail,
	java_call,
	call,
	
	ret, // 12
	halt,
	atom,
	car,
	
	cdr, // 16
	cons,
	eq,
	neq,
	
	and, // 20
	or,
	not,
	add,
	
	sub, // 24
	mul,
	div,
	mod,
	
	gt, // 28
	ge,
	lt,
	le;
//	rem,
//	dup;
	
	private static final List<Instruction> INST_CACHES = LambdaUtils.select(Arrays.asList(values()),
			new Func1<Instruction, InstructionSetEnum>() {
		@Override
		public Instruction call(InstructionSetEnum inst) {
			return new Instruction(inst);
		}
	});
		
	public final Instruction create() { return INST_CACHES.get(ordinal()); }
	
	public final Instruction create(Serializable... args) { return new Instruction(this, args); }

	public static boolean contains(String op) {
		try {
			valueOf(op);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
