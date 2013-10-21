package ash.vm;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import bruce.downloader.framework.utils.CommonUtils;
import ash.parser.ListUtils;
import ash.parser.Node;

public final class VMFrame implements Serializable {
	private static final long serialVersionUID = 3322385890943332297L;
	
	public static final Map<String, Serializable> tempVar = VM.tempVar;
	private Scope myScope;
	public Serializable[] callArgs;
	
	public final Deque<Serializable> workingStack = new ArrayDeque<>();
	public final List<Instruction> executingInsts;
	public int runIndex;
	VMFrame prevFrame;
	VMFrame nextFrame;

	public VMFrame(List<Instruction> executingInstructions, Scope parentScope) {
		executingInsts = executingInstructions;
		myScope = parentScope;
	}

	private void pushWorkingStack(Serializable ser) { workingStack.push(ser); }

	public Serializable popWorkingStack() { return workingStack.pop(); }

	public void prepareNextFrame(Closure closure, int paramsCount) {
		nextFrame = new VMFrame(closure.ins, closure.env);
		nextFrame.callArgs = createCallingArgs(paramsCount);
	}

	private Serializable[] createCallingArgs(int paramsCount) {
		Serializable[] args = new Serializable[paramsCount];
		for (int i = args.length - 1; -1 < i; i--)
			args[i] = popWorkingStack();
		return args;
	}

	private Closure makeSubClosure(List<Instruction> rtn) {
		return new Closure(rtn, new Scope(myScope, callArgs));
	}

	public void execUntilStackChange() {
		while (nextFrame == null && prevFrame != null) {
			Instruction i = executingInsts.get(runIndex++);
			if (CommonUtils.DEBUGGING) {
				if (i.args != null)
					System.out.print(CommonUtils.buildString(makeIndent(this), i.ins, ' ', CommonUtils.displayArray(i.args, " ")));
				else
					System.out.print(CommonUtils.buildString(makeIndent(this), i.ins));
			}
			
			exec(i.ins, i.args);
			
			if (CommonUtils.DEBUGGING) {
				System.out.print('\t');
				System.out.println(workingStack);
			}
		}

	}
	
	@SuppressWarnings("unchecked")
	private void exec(InstructionSetEnum ins, Serializable[] args) {
		int ordinal = ins.ordinal();
		if (ordinal < 16) {
			if (ordinal < 8) {
				if (ordinal < 4) { // 0...4
					if (ordinal < 2) {
						if (ordinal == 0) { // ldp
							int varIndex = (Integer) args[0]; // params
							if (varIndex < callArgs.length)
								pushWorkingStack(callArgs[varIndex]);
							else
								pushWorkingStack(myScope.queryArg(varIndex - callArgs.length));
						} else { // ldv
							pushWorkingStack(tempVar.get(args[0])); // symbol
						}
					} else { // ldc, quote
						pushWorkingStack(args[0]);
					}
				} else { // 4...8
					if (ordinal < 6) {
						if (ordinal == 4) { //asn
							if (tempVar.containsKey((String) args[0]))
								throw new IllegalArgumentException("Already Define:" + args[0]);
							tempVar.put((String) args[0], popWorkingStack());
						} else { // cons_args
							int dotIndex = (Integer) args[0];
							callArgs[dotIndex] = ListUtils.toNode(dotIndex, callArgs);
							if (dotIndex + 1 < callArgs.length) {
								Serializable[] sorterArgs = new Serializable[dotIndex + 1];
								System.arraycopy(callArgs, 0, sorterArgs, 0, sorterArgs.length);
								callArgs = sorterArgs;
							}
						}
					} else {
						if (ordinal == 6) { // closure
							pushWorkingStack(makeSubClosure((List<Instruction>) args[0]));
						} else { // jmp
							runIndex = (Integer) args[0];
						}
					}
				}
			} else {
				if (ordinal < 12) { // 8...12
					if (ordinal < 10) {
						if (ordinal == 8) { // jz
							Serializable pop = popWorkingStack();
							if (pop == Node.NIL) runIndex = (Integer) args[0];
						} else { // tail TODO tail + cons_args => ?
							callArgs = createCallingArgs((Integer) args[0]);
							workingStack.clear();
							runIndex = 0;
						}
					} else {
						if (ordinal == 10) { // java_call
							pushWorkingStack(((JavaMethod) popWorkingStack()).call(createCallingArgs((Integer) args[0])));
						} else { // call
							Serializable func = popWorkingStack();
							if (func instanceof Closure) { // symbol
								prepareNextFrame((Closure) func, (Integer) args[0]);
							} else if (func instanceof InstructionSetEnum) { // instruction
								exec((InstructionSetEnum) func, args);
							} else { // java method
								pushWorkingStack(((JavaMethod) func).call(createCallingArgs((Integer) args[0])));
							}
						}
					}
				} else { // 12...16
					if (ordinal < 14) {
						if (ordinal == 12) { // ret
							prevFrame.pushWorkingStack(popWorkingStack());
							prevFrame = null;
						} else { // halt
							if (workingStack.isEmpty())
								pushWorkingStack(Node.NIL);
							if (prevFrame != null)
								prevFrame.pushWorkingStack(popWorkingStack());
							prevFrame = null;
						}
					} else {
						if (ordinal == 14) { //atom
							pushWorkingStack(ListUtils.atom(popWorkingStack()));
						} else { // car
							pushWorkingStack(ListUtils.car((Node) popWorkingStack()));
						}
					}
				}
			}
		} else {
			if (ordinal < 24) {
				if (ordinal < 20) { // 16...20
					if (ordinal < 18) {
						if (ordinal == 16) { //cdr
							pushWorkingStack(ListUtils.cdr((Node) popWorkingStack()));
						} else { // cons
							Serializable elem2 = popWorkingStack();
							Serializable elem = popWorkingStack();
							pushWorkingStack(ListUtils.cons(elem, (Node) elem2));
						}
					} else {
						if (ordinal == 18) { // eq
							Serializable elem2 = popWorkingStack();
							Serializable elem = popWorkingStack();
							pushWorkingStack(ListUtils.eq(elem, elem2));
						} else { // neq
							Serializable elem2 = popWorkingStack();
							Serializable elem = popWorkingStack();
							pushWorkingStack(ListUtils.transformBoolean(ListUtils.eq(elem, elem2) == Node.NIL));
						}
					}
				} else { // 20...24
					if (ordinal < 22) {
						if (ordinal == 20) { // and
							Serializable elem2 = popWorkingStack();
							Serializable elem = popWorkingStack();
							pushWorkingStack(ListUtils.transformBoolean(elem != Node.NIL && elem2 != Node.NIL));
						} else { // or
							Serializable elem2 = popWorkingStack();
							Serializable elem = popWorkingStack();
							pushWorkingStack(ListUtils.transformBoolean(elem != Node.NIL || elem2 != Node.NIL));
						}
					} else {
						if (ordinal == 22) { // not
							Serializable elem = popWorkingStack();
							pushWorkingStack(ListUtils.transformBoolean(elem == Node.NIL));
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
	
	protected static final String makeIndent(VMFrame headFrame) {
		int indent = calcIndent(headFrame, -1);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; i++)
			sb.append('\t');
		return sb.toString();
	}

	protected static final int calcIndent(VMFrame headFrame, int base) {
		if (headFrame.prevFrame == null) return base;
		return calcIndent(headFrame.prevFrame, base + 1);
	}
}
