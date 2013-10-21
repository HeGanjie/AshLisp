package ash.vm;

import java.io.Serializable;


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
	
	public final Instruction create() { return new Instruction(this); }
	
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
