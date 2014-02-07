package ash.vm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ash.lang.ListUtils;
import bruce.common.functional.Func1;
import bruce.common.functional.LambdaUtils;


public enum InstructionSet {
	ldp, // 0
	ldv,
	ldc,
	ldt,
	
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
	
	private static final Set<String> INST_NAME_CACHE = new HashSet<>(LambdaUtils.select(Arrays.asList(values()),
			new Func1<String, InstructionSet>() {
		@Override
		public String call(InstructionSet ins) { return ins.name(); }
	}));
	
	private static final List<Instruction> INST_CACHES = LambdaUtils.select(Arrays.asList(values()),
			new Func1<Instruction, InstructionSet>() {
		@Override
		public Instruction call(InstructionSet inst) {
			return new Instruction(inst);
		}
	});
		
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
