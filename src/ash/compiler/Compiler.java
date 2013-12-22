package ash.compiler;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ash.lang.ISeq;
import ash.lang.ListUtils;
import ash.lang.MacroExpander;
import ash.lang.Node;
import ash.vm.Instruction;
import ash.vm.InstructionSetEnum;
import ash.vm.JavaMethod;

public final class Compiler {
	private static final String MULTI_ARGS_SIGNAL = ".";
	private static final Set<String> NORMAL_INSTRUCTION_SET = new HashSet<>(Arrays.asList(
			"def", "quote", "cond", "lambda",
			"atom", "car", "cdr", "not",
			"neq", "eq", "cons", "add", "sub", "mul", "div", "mod",
			"gt", "ge", "lt", "le", "and", "or"));
	
	private Compiler() {}
	
	public static Node astsToInsts(ISeq parseResult) {
		if (parseResult == Node.NIL) return Node.NIL;
		Serializable instNode = compile(parseResult.head(), Node.NIL, 0);
		if (!(instNode instanceof Node)) {
			instNode = new Node(instNode); // ensure no raw instructions in the returned list
		}
		return new Node(instNode, astsToInsts(parseResult.rest()));
	}
	
	private static Serializable compile(final Serializable exp, Node lambdaArgs, int startIndex) {
		if (exp instanceof Node) {
			Node node = (Node) exp; // (operation ...)
			final Serializable op = node.head();
			if (NORMAL_INSTRUCTION_SET.contains(op)) {
				switch (((String) op).toLowerCase()) {
				case "def":
					return listInstruction(
							compile(node.rest().rest().head(), lambdaArgs, startIndex),
							InstructionSetEnum.asn.create(node.rest().head()));
				case "quote":
					return listInstruction(InstructionSetEnum.quote.create(node.rest().head()));
				case "cond":
					return compileCond(node.rest(), lambdaArgs, startIndex);
				case "lambda":
					int dotIndex = ListUtils.indexOf((Node) node.rest().head(), MULTI_ARGS_SIGNAL, 0);
					boolean notCombineArgs = dotIndex == -1;
					return listInstruction(
							InstructionSetEnum.closure.create(
									expand(listInstructionRecur(notCombineArgs ? 1 : 0,
											InstructionSetEnum.cons_args.create(dotIndex),
											compile(node.rest().rest().head(),
													ListUtils.append((Node) node.rest().head(), lambdaArgs),
													notCombineArgs ? 0 : 1),
													InstructionSetEnum.ret.create()))));
				default:
					return listInstruction(
							compileArgs(node.rest(), lambdaArgs, startIndex),
							InstructionSetEnum.valueOf((String) op).create());
				}
			} else if (op instanceof String && MacroExpander.hasMacro((String) op, node)) {
				return compile(MacroExpander.expand(node), lambdaArgs, startIndex);
			} else { // (func ...) | (.str ...) | ((lambda ...) ...) | (closure@1a2b3c ...) <- only adapt for this situation (apply + '(...))
				int argsCount = ListUtils.count(node.rest());
				InstructionSetEnum callMethod = op instanceof String && ((String) op).charAt(0) == '.'
						? InstructionSetEnum.java_call
						: InstructionSetEnum.call;
				return listInstruction(
						compileArgs(node.rest(), lambdaArgs, startIndex),
						compile(op, lambdaArgs, startIndex),
						callMethod.create(argsCount));
			}
		} else if (exp instanceof String) {
			return compileSymbol(exp, lambdaArgs); // (... abc "a" add .puts ...)
		} else
			return InstructionSetEnum.ldc.create(exp); // (... 1 2 3.4 \a ...)
	}

	protected static Serializable compileSymbol(final Serializable exp, Node lambdaArgs) {
		String op = (String) exp;
		if (op.charAt(0) == '\"' && op.charAt(op.length() - 1) == '\"')
			return InstructionSetEnum.ldc.create(op.substring(1, op.length() - 1)); // String
		else if (op.charAt(0) == '.')
			return InstructionSetEnum.ldc.create(JavaMethod.create(op.substring(1))); // java method

		int symbolIndexOfArgs = findArgIndex(lambdaArgs, exp);
		if (symbolIndexOfArgs == -1) {
			if (InstructionSetEnum.contains(op))
				return InstructionSetEnum.ldc.create(InstructionSetEnum.valueOf(op).create()); // instruction
			else
				return InstructionSetEnum.ldv.create(op); // symbol
		} else {
			return InstructionSetEnum.ldp.create(symbolIndexOfArgs); // symbol index of params
		}
	}

	protected static int findArgIndex(Node lambdaArgs, final Serializable op) {
		assert(!MULTI_ARGS_SIGNAL.equals(op));
		int dotIndex = ListUtils.indexOf(lambdaArgs, MULTI_ARGS_SIGNAL, 0);
		int opPos = ListUtils.indexOf(lambdaArgs, op, 0);
		if (dotIndex < 0)
			return opPos;
		else
			return Math.min(dotIndex, opPos);
	}

	public static Serializable expand(Serializable instrNodes) {
		//make sure only Instruction List appear in runtime
		return (Serializable) ((Node) instrNodes).toList(Instruction.class);
	}
	
	private static Node compileArgs(ISeq args, Node lambdaArgs, int startIndex) {
		if (args == Node.NIL) return Node.NIL;
		return listInstruction(compile(args.head(), lambdaArgs, startIndex), compileArgs(args.rest(), lambdaArgs, startIndex));
	}

	private static Serializable compileCond(ISeq pairList, Node lambdaArgs, int startIndex) {
		if (pairList == Node.NIL) return listInstruction(InstructionSetEnum.quote.create(Node.NIL));
		
		Node headPair = (Node) pairList.head();
		
		Serializable condition = compile(headPair.head(), lambdaArgs, startIndex);
		int condInstCount = countInstruction(condition) + 1;
		
		Serializable exp = compile(headPair.rest().head(), lambdaArgs, startIndex + condInstCount);
		int nextCaseStartIndex = startIndex + condInstCount + countInstruction(exp) + 1;
		
		Serializable nextCase = compileCond(pairList.rest(), lambdaArgs, nextCaseStartIndex);
		
		return listInstruction(
				condition,
				InstructionSetEnum.jz.create(nextCaseStartIndex),
				exp,
				InstructionSetEnum.jmp.create(nextCaseStartIndex + countInstruction(nextCase)),
				nextCase);
	}

	private static int countInstruction(Serializable ins) {
		if (ins instanceof Instruction) return 1;
		return ListUtils.count((Node) ins);
	}
	
	public static Node listInstruction(Serializable... ins) { return listInstructionRecur(0, ins); }

	public static Node listInstructionRecur(int skipParams, Serializable... ins) {
		if (ins.length == skipParams) return Node.NIL;
		Serializable s = ins[skipParams];
		if (s instanceof Node)
			return ListUtils.append((Node) s, listInstructionRecur(skipParams + 1, ins));
		else
			return new Node(s, listInstructionRecur(skipParams + 1, ins));
	}
}
