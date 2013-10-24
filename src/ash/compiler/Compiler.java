package ash.compiler;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import bruce.common.functional.LambdaUtils;
import ash.parser.ListUtils;
import ash.parser.Node;
import ash.vm.Instruction;
import ash.vm.InstructionSetEnum;
import ash.vm.JavaMethod;

public final class Compiler {
	private static final String MULTI_ARGS_SIGNAL = ".";
	private static final Node DEFAULT_CASE = new Node("quote", new Node(Node.T));
	private static final Set<String> NORMAL_INSTRUCTION_SET = new HashSet<>(Arrays.asList(
			"def", "quote", "cond", "lambda",
			"atom", "car", "cdr", "not",
			"neq", "eq", "cons", "add", "sub", "mul", "div", "mod",
			"gt", "ge", "lt", "le", "and", "or"));
	
	private Compiler() {}
	
	public static Node astsToInsts(Node parseResult) {
		if (parseResult == Node.NIL) return Node.NIL;
		Serializable instNode = compile(parseResult.left, null, Node.NIL, 0);
		if (!(instNode instanceof Node)) {
			instNode = new Node(instNode); // ensure no raw instructions in the returned list
		}
		return new Node(instNode, astsToInsts(parseResult.next));
	}
	
	// var tailRecurFuncName is only for tail recursion optimize
	private static Serializable compile(final Serializable exp, String tailRecurFuncName, Node lambdaArgs, int startIndex) {
		if (exp instanceof Node) {
			Node node = (Node) exp; // (operation ...)
			final Serializable op = node.left;
			if (NORMAL_INSTRUCTION_SET.contains(op)) {
				switch (((String) op).toLowerCase()) {
				case "def":
					return listInstruction(
							compile(node.next.next.left, (String) node.next.left, lambdaArgs, startIndex),
							InstructionSetEnum.asn.create(node.next.left));
				case "quote":
					return listInstruction(InstructionSetEnum.quote.create(node.next.left));
				case "cond":
					return compileCond(node.next, tailRecurFuncName, lambdaArgs, startIndex);
				case "lambda":
					int dotIndex = ListUtils.indexOf((Node) node.next.left, MULTI_ARGS_SIGNAL, 0);
					boolean notCombineArgs = dotIndex == -1;
					return listInstruction(InstructionSetEnum.closure.create(
							expand(listInstructionRecur(notCombineArgs ? 1 : 0,
									InstructionSetEnum.cons_args.create(dotIndex),
									compile(node.next.next.left,
											tailRecurFuncName,
											ListUtils.append((Node) node.next.left, lambdaArgs),
											notCombineArgs ? 0 : 1),
									InstructionSetEnum.ret.create()))));
				default:
					return listInstruction(
							compileArgs(node.next, lambdaArgs, startIndex),
							InstructionSetEnum.valueOf((String) op).create());
				}
			} else { // (+ ...) | (.str ...) | ((lambda ...) ...) | (closure@1a2b3c ...) <- only adapt for this situation (apply + '(...))
				int argsCount = LambdaUtils.count(node.next, null);
				InstructionSetEnum callMethod = op.equals(tailRecurFuncName)
						? InstructionSetEnum.tail // tail recursion optimize
						: op instanceof String && op.toString().charAt(0) == '.'
							? InstructionSetEnum.java_call
							: InstructionSetEnum.call;
				return listInstruction(
						compileArgs(node.next, lambdaArgs, startIndex),
						callMethod == InstructionSetEnum.tail ? Node.NIL : compile(op, null, lambdaArgs, startIndex),
						callMethod.create(argsCount));
			}
		} else if (exp instanceof String) { // (... abc "a" add .puts ...)
			String op = (String) exp;
			if (op.charAt(0) == '\"' && op.charAt(op.length() - 1) == '\"')
				return InstructionSetEnum.ldc.create(op.substring(1, op.length() - 1)); // String
			else if (op.charAt(0) == '.')
				return InstructionSetEnum.ldc.create(new JavaMethod(op.substring(1))); // java method

			int symbolIndexOfArgs = findArgIndex(lambdaArgs, exp);
			if (symbolIndexOfArgs == -1) {
				if (InstructionSetEnum.contains(op))
					return InstructionSetEnum.ldc.create(InstructionSetEnum.valueOf(op)); // instruction
				else
					return InstructionSetEnum.ldv.create(op); // symbol
			} else {
				return InstructionSetEnum.ldp.create(symbolIndexOfArgs); // symbol index of params
			}
		} else // (... 1 2 3.4 ...)
			return InstructionSetEnum.ldc.create(exp);
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
	
	private static Node compileArgs(Node args, Node lambdaArgs, int startIndex) {
		if (args == Node.NIL) return Node.NIL;
		return listInstruction(compile(args.left, null, lambdaArgs, startIndex), compileArgs(args.next, lambdaArgs, startIndex));
	}

	private static Serializable compileCond(Node pairList, String tailRecurFuncName, Node lambdaArgs, int startIndex) {
		if (pairList == Node.NIL) return listInstruction(InstructionSetEnum.quote.create(Node.NIL));
		
		Node headPair = (Node) pairList.left;
		boolean isDefaultCase = DEFAULT_CASE.equals(headPair.left);
		
		Serializable condition = isDefaultCase ? Node.NIL : compile(headPair.left, null, lambdaArgs, startIndex);
		int condInstCount = isDefaultCase ? 0 : countInstruction(condition) + 1;
		
		Serializable exp = compile(headPair.next.left, tailRecurFuncName, lambdaArgs, startIndex + condInstCount);
		int nextCaseStartIndex = startIndex + condInstCount + countInstruction(exp) + 1;
		
		Serializable nextCase = isDefaultCase ? Node.NIL : compileCond(pairList.next, tailRecurFuncName, lambdaArgs, nextCaseStartIndex);
		
		return listInstructionRecur(isDefaultCase ? 2 : 0,
				condition,
				InstructionSetEnum.jz.create(nextCaseStartIndex),
				exp,
				InstructionSetEnum.jmp.create(nextCaseStartIndex + countInstruction(nextCase)),
				nextCase);
	}

	private static int countInstruction(Serializable ins) {
		if (ins instanceof Instruction) return 1;
		return LambdaUtils.count((Node) ins, null);
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
