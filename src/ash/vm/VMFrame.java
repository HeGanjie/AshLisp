package ash.vm;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import ash.lang.BasicType;
import ash.lang.ListUtils;
import ash.lang.PersistentList;
import ash.lang.Symbol;
import ash.util.JavaUtils;

public final class VMFrame implements Serializable {
	private static final Symbol SLEEP_SYMBOL = Symbol.create("_sleep_");
	private static final long serialVersionUID = 3322385890943332297L;
	private static final InstructionSet[] INST_ARR = InstructionSet.values();
	
	private static final Map<Symbol, Object> tempVar = VM.tempVar;
	
	private final Closure source;
	private final Deque<Object> workingStack = new ArrayDeque<>();
	private final List<Instruction> executingInsts;
	private final Scope myScope;
	Object[] callArgs;
	private int runIndex;
	boolean frameChanged = false;
	VMFrame prevFrame;
	VMFrame nextFrame;

	public VMFrame(List<Instruction> executingInstructions, Scope parentScope) {
		source = null;
		executingInsts = executingInstructions;
		myScope = parentScope;
	}

	public VMFrame(Closure closure) {
		source = closure;
		executingInsts = closure.getInsts();
		myScope = closure.env;
	}

	private void pushWorkingStack(Object ser) { workingStack.push(ser); }

	public Object popWorkingStack() { return workingStack.pop(); }
	
	public Object popReturnValue() { return workingStack.isEmpty() ? BasicType.NIL : workingStack.pop(); }

	private void prepareNextFrame(Closure closure, int paramsCount) {
		nextFrame = new VMFrame(closure);
		nextFrame.callArgs = createCallingArgs(paramsCount);
		frameChanged = true;
	}

	private Object[] createCallingArgs(int paramsCount) {
		Object[] args = new Object[paramsCount];
		for (int i = args.length - 1; -1 < i; i--)
			args[i] = popWorkingStack();
		return args;
	}

	private Closure makeSubClosure(ClosureArgs args) {
		Scope closureScope = callArgs == null ? null : new Scope(myScope, callArgs);
		return new Closure(args, closureScope);
	}

	public void execUntilStackChange_DEBUG() {
		while (!frameChanged) {
			Instruction i = executingInsts.get(runIndex++);
			if (i.args != null)
				System.out.print(JavaUtils.buildString(makeIndent(this), INST_ARR[i.ins], ' ', i.args));
			else
				System.out.print(JavaUtils.buildString(makeIndent(this), INST_ARR[i.ins]));

			exec(i.ins, i.args);

			System.out.print('\t');
			System.out.println(workingStack);
			
			if (tempVar.containsKey(SLEEP_SYMBOL))
                JavaUtils.delay(((Number) tempVar.get(SLEEP_SYMBOL)).longValue());
		}
	}
	
	public void execUntilStackChange() {
		while (!frameChanged) {
			Instruction i = executingInsts.get(runIndex++);
			try {
				exec(i.ins, i.args);
			} catch (Throwable e) {
				throw new RuntimeException("Crash " + getFrameTrace(), e);
			}
		}
	}

	private String getFrameTrace() {
		String msg = prevFrame != null ? prevFrame.getFrameTrace() + "\n\n" : "";
		return msg + "at: " + executingInsts.get(runIndex - 1)
				+ "\nClosure: " + source
				+ "\nArgs: " + JavaUtils.displayArray(callArgs, ", ")
				+ "\nrunning index " + (runIndex - 1) + " of " + executingInsts;
	}
	
	private void exec(int ordinal, Object arg) {
		if (ordinal < 16) {
			if (ordinal < 8) {
				if (ordinal < 4) { // 0...4
					if (ordinal < 2) {
						if (ordinal == 0) { // ldp
							int varIndex = (Integer) arg;
							Object val = varIndex < callArgs.length
									? callArgs[varIndex]
									: myScope.queryArg(varIndex - callArgs.length);
							pushWorkingStack(val);
						} else { // ldv
							Object var = tempVar.get(arg);
							if (var == null)
								throw new RuntimeException("Undefine var:" + arg);
							pushWorkingStack(var); // symbol
						}
					} else { // ldc (quote)
						pushWorkingStack(arg);
					}
				} else { // 4...8
					if (ordinal < 6) {
						if (ordinal == 4) { //asn
							if (tempVar.containsKey(arg))
								System.out.println("Warnning: Redefining " + arg);
							tempVar.put((Symbol) arg, popWorkingStack());
						} else { // cons_args
							int dotIndex = (Integer) arg;
							Object[] newArgs = new Object[dotIndex + 1];
							System.arraycopy(callArgs, 0, newArgs, 0, dotIndex);
							newArgs[dotIndex] = ListUtils.toSeq(dotIndex, callArgs);
							callArgs = newArgs;
						}
					} else {
						if (ordinal == 6) { // closure
							pushWorkingStack(makeSubClosure((ClosureArgs) arg));
						} else { // jmp
							runIndex = (Integer) arg;
						}
					}
				}
			} else {
				if (ordinal < 12) { // 8...12
					if (ordinal < 10) {
						if (ordinal == 8) { // jz
							if (!ListUtils.transformBoolean(popWorkingStack()))
								runIndex = (Integer) arg;
						} else { // tail
							callArgs = createCallingArgs((Integer) arg);
							workingStack.clear();
							runIndex = 0;
						}
					} else {
						if (ordinal == 10) { // java_call
							pushWorkingStack(((JavaMethod) popWorkingStack()).call(createCallingArgs((Integer) arg)));
						} else { // call
							Object func = popWorkingStack();
							if (func instanceof Closure) { // symbol
								prepareNextFrame((Closure) func, (Integer) arg);
							} else if (func instanceof Instruction) { // instruction
								exec(((Instruction) func).ins, arg);
							} else if (func instanceof JavaMethod) { // java method
								pushWorkingStack(((JavaMethod) func).call(createCallingArgs((Integer) arg)));
							} else {
								throw new RuntimeException("Undefined method:" + func);
							}
						}
					}
				} else { // 12...16
					if (ordinal < 14) {
						if (ordinal == 12) { // ret
							prevFrame.pushWorkingStack(popWorkingStack());
							prevFrame.frameChanged = false;
							prevFrame = null;
						} else { // halt
							if (workingStack.isEmpty())
								pushWorkingStack(BasicType.NIL);
							if (prevFrame != null) {
								prevFrame.pushWorkingStack(popWorkingStack());
								prevFrame.frameChanged = false;
								prevFrame = null;
							}
						}
						frameChanged = true;
					} else {
						if (ordinal == 14) { //eqv
							pushWorkingStack(ListUtils.transformBoolean(popWorkingStack() == popWorkingStack()));
						} else { // car
							pushWorkingStack(ListUtils.car(PersistentList.cast(popWorkingStack())));
						}
					}
				}
			}
		} else {
			if (ordinal < 24) {
				if (ordinal < 20) { // 16...20
					if (ordinal < 18) {
						if (ordinal == 16) { //cdr
							pushWorkingStack(ListUtils.cdr(PersistentList.cast(popWorkingStack())));
						} else { // cons
							Object elem2 = popWorkingStack();
							Object elem = popWorkingStack();
							pushWorkingStack(ListUtils.cons(elem, PersistentList.cast(elem2)));
						}
					} else {
						if (ordinal == 18) { // eq
							Object elem2 = popWorkingStack();
							Object elem = popWorkingStack();
							pushWorkingStack(ListUtils.eq(elem, elem2));
						} else { // neq
							Object elem2 = popWorkingStack();
							Object elem = popWorkingStack();
							pushWorkingStack(ListUtils.transformBoolean(!elem.equals(elem2)));
						}
					}
				} else { // 20...24
					if (ordinal < 22) {
						if (ordinal == 20) { // land
							boolean elem2 = ListUtils.transformBoolean(popWorkingStack());
							boolean elem = ListUtils.transformBoolean(popWorkingStack());
							pushWorkingStack(ListUtils.transformBoolean(elem && elem2));
						} else { // lor
							boolean elem2 = ListUtils.transformBoolean(popWorkingStack());
							boolean elem = ListUtils.transformBoolean(popWorkingStack());
							pushWorkingStack(ListUtils.transformBoolean(elem || elem2));
						}
					} else {
						if (ordinal == 22) { // not
							boolean elem = ListUtils.transformBoolean(popWorkingStack());
							pushWorkingStack(ListUtils.transformBoolean(!elem));
						} else { // add
							Number n2 = (Number) popWorkingStack();
							Number n1 = (Number) popWorkingStack();
							pushWorkingStack(Maths.add(n1, n2));
						}
					}
				}
			} else {
				if (ordinal < 28) { // 24...28
					if (ordinal < 26) {
						if (ordinal == 24) { // sub
							Number n2 = (Number) popWorkingStack();
							Number n1 = (Number) popWorkingStack();
							pushWorkingStack(Maths.subtract(n1, n2));
						} else { // mul
							Number n2 = (Number) popWorkingStack();
							Number n1 = (Number) popWorkingStack();
							pushWorkingStack(Maths.multiply(n1, n2));
						}
					} else {
						if (ordinal == 26) { // div
							Number n2 = (Number) popWorkingStack();
							Number n1 = (Number) popWorkingStack();
							pushWorkingStack(Maths.divide(n1, n2));
						} else { // mod
							Number n2 = (Number) popWorkingStack();
							Number n1 = (Number) popWorkingStack();
							pushWorkingStack(Maths.modulus(n1, n2));
						}
					}
				} else { // 28...32
					if (ordinal < 30) {
						if (ordinal == 28) { // gt
							Number n2 = (Number) popWorkingStack();
							Number n1 = (Number) popWorkingStack();
							pushWorkingStack(ListUtils.transformBoolean(Maths.greaterThan(n1, n2)));
						} else { // ge
							Number n2 = (Number) popWorkingStack();
							Number n1 = (Number) popWorkingStack();
							pushWorkingStack(ListUtils.transformBoolean(Maths.greaterEqual(n1, n2)));
						}
					} else {
						if (ordinal == 30) { // lt
							Number n2 = (Number) popWorkingStack();
							Number n1 = (Number) popWorkingStack();
							pushWorkingStack(ListUtils.transformBoolean(Maths.lessThan(n1, n2)));
						} else { // le
							Number n2 = (Number) popWorkingStack();
							Number n1 = (Number) popWorkingStack();
							pushWorkingStack(ListUtils.transformBoolean(Maths.lessEqual(n1, n2)));
						}
					}
				}
			}
		}
	}
	
	private static final String makeIndent(VMFrame headFrame) {
		int indent = calcIndent(headFrame, -1);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; i++)
			sb.append('\t');
		return sb.toString();
	}

	private static final int calcIndent(VMFrame headFrame, int base) {
		if (headFrame.prevFrame == null) return base;
		return calcIndent(headFrame.prevFrame, base + 1);
	}
}
