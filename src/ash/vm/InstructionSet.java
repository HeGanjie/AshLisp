package ash.vm;

import ash.lang.ListUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum InstructionSet {
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
	
	private static final Set<String> INST_NAME_CACHE = Arrays.asList(values()).stream().map(InstructionSet::name).collect(Collectors.toSet());

	private static final List<Instruction> INST_CACHES = Arrays.asList(values()).stream().map(Instruction::new).collect(Collectors.toList());

	public final Instruction create() { return INST_CACHES.get(ordinal()); }
	
	public final Instruction create(Object arg) {
		return new Instruction(this, arg);
	}
	
	public final Instruction create(Object arg0, Object arg1) {
		return new Instruction(this, ListUtils.toSeq(0, arg0, arg1));
	}

	public static boolean contains(String op) {
		return INST_NAME_CACHE.contains(op);
	}
}
