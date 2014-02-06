package ash.compiler;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ash.lang.BasicType;
import ash.lang.ListUtils;
import ash.lang.MacroExpander;
import ash.lang.Node;
import ash.lang.PersistentList;
import ash.lang.Symbol;
import ash.vm.Instruction;
import ash.vm.InstructionSetEnum;
import ash.vm.JavaMethod;
import bruce.common.functional.Func1;

public final class Compiler {
	public static final Symbol MULTI_ARGS_SIGNAL = Symbol.create(".");
	private static final Set<String> NORMAL_INSTRUCTION_SET = new HashSet<>(Arrays.asList(
			"def", "quote", "cond", "lambda",
			"atom", "car", "cdr", "not",
			"neq", "eq", "cons", "add", "sub", "mul", "div", "mod",
			"gt", "ge", "lt", "le", "and", "or"));
	
	private Compiler() {}
	
	public static PersistentList compileSingle(Object expAst) {
		return (PersistentList) compile(expAst, BasicType.NIL, 0);
	}
	
	public static PersistentList batchCompile(PersistentList parseResult) {
		if (parseResult.isEndingNode()) return BasicType.NIL;
		return new Node(compileSingle(parseResult.head()), batchCompile(parseResult.rest()));
	}

	private static Serializable compile(final Object exp, PersistentList lambdaArgs, int startIndex) {
		if (exp instanceof Node) {
			return compileNode((Node) exp, lambdaArgs, startIndex);
		} else {
			return compileObject(exp, lambdaArgs);
		}
	}

	private static Serializable compileObject(final Object exp, PersistentList lambdaArgs) {
		if (exp instanceof Symbol)
			return listInstruction(compileSymbol((Symbol) exp, lambdaArgs)); // (... a add puts ...)
		else
			return listInstruction(InstructionSetEnum.ldc.create(exp)); // (... 1 2 3.4 \a "b" closure ...)
	}

	private static Serializable compileNode(Node node, PersistentList lambdaArgs, int startIndex) {
		final Object op = node.head(); // (operation ...)
		if (op instanceof Symbol && NORMAL_INSTRUCTION_SET.contains(((Symbol) op).name)) {
			switch (op.toString()) {
			case "def":
				return listInstruction(
						compile(node.third(), lambdaArgs, startIndex),
						InstructionSetEnum.asn.create(node.second()));
			case "quote":
				return listInstruction(InstructionSetEnum.ldc.create(node.second()));
			case "cond":
				return compileCond(node.rest(), lambdaArgs, startIndex);
			case "lambda":
				return compileLambda(node, lambdaArgs);
			default:
				return compileInstCall((Symbol) op, node.rest(), lambdaArgs, startIndex);
			}
		} else if (MacroExpander.SYNTAX_QUOTE.equals(op)) {// `(...)
			return compile(MacroExpander.visitSyntaxQuote(node.second()), lambdaArgs, startIndex);
		} else if (op instanceof Symbol && MacroExpander.hasMacro(node)) { // (let ...)
			return compile(MacroExpander.expand(node), lambdaArgs, startIndex);
		} else { // (func ...) | (.new ...) | ((lambda ...) ...) | (closure ...)
			return compileCall(op, node.rest(), lambdaArgs, startIndex);
		}
	}

	private static Serializable compileLambda(Node node, PersistentList lambdaArgs) {
		PersistentList paramList = (PersistentList) node.second();
		int dotIndex = ListUtils.indexOf(paramList, MULTI_ARGS_SIGNAL, 0);
		boolean notCombineArgs = dotIndex == -1;
		PersistentList argsList = notCombineArgs ? paramList : removeDot(paramList);
		return listInstruction(
				InstructionSetEnum.closure.create(
						expand(listInstructionRecur(notCombineArgs ? 1 : 0,
								InstructionSetEnum.cons_args.create(dotIndex),
								compile(node.third(),
										ListUtils.append(argsList, lambdaArgs),
										notCombineArgs ? 0 : 1),
										InstructionSetEnum.ret.create())),
						node));
	}

	private static Serializable compileCall(Object op, PersistentList argList, PersistentList lambdaArgs, int startIndex) {
		int argsCount = ListUtils.count(argList);
		InstructionSetEnum callMethod = op instanceof Symbol && isJavaCallSymbol(((Symbol) op).name)
				? InstructionSetEnum.java_call
				: InstructionSetEnum.call;
		return listInstruction(
				compileArgs(argList, lambdaArgs, startIndex),
				compile(op, lambdaArgs, startIndex),
				callMethod.create(argsCount));
	}

	private static PersistentList compileInstCall(Symbol inst, PersistentList argList, PersistentList lambdaArgs, int startIndex) {
		return listInstruction(
				compileArgs(argList, lambdaArgs, startIndex),
				InstructionSetEnum.valueOf(inst.name).create());
	}

	private static PersistentList removeDot(PersistentList seq) {
		return ListUtils.filter(seq, new Func1<Boolean, Object>() {
			@Override
			public Boolean call(Object symbol) { return !MULTI_ARGS_SIGNAL.equals(symbol); }
		});
	}

	protected static Serializable compileSymbol(final Symbol symbol, PersistentList lambdaArgs) {
		String methodName = symbol.name;
		if (isJavaCallSymbol(methodName))
			return InstructionSetEnum.ldc.create(JavaMethod.create(symbol)); // java method
		else if (isJavaClassPathSymbol(methodName))
			return InstructionSetEnum.ldc.create(symbol); // java class path

		PersistentList argIndexs = findArgIndexs(lambdaArgs, symbol, 0, BasicType.NIL);
		if (argIndexs.isEndingNode()) {
			if (InstructionSetEnum.contains(methodName))
				return InstructionSetEnum.ldc.create(InstructionSetEnum.valueOf(methodName).create()); // instruction
			else
				return InstructionSetEnum.ldv.create(symbol); // symbol
		} else if (argIndexs.rest().isEndingNode())
			return InstructionSetEnum.ldp.create(argIndexs.head()); // symbol index of params
		else
			return InstructionSetEnum.ldt.create(argIndexs); // destructuring
	}

	private static PersistentList findArgIndexs(PersistentList lambdaArgs, Symbol symbol, int skiped, Node stack) {
		if (lambdaArgs.isEndingNode()) return BasicType.NIL;
		Object val = lambdaArgs.head();
		if (val instanceof Node) {
			PersistentList rst = findArgIndexs((PersistentList) val, symbol, 0, new Node(skiped, stack));
			if (!rst.isEndingNode()) return rst;
		} else if (MULTI_ARGS_SIGNAL.equals(val)) {
			if (symbol.equals(lambdaArgs.second())) {
				return new Node(-skiped, stack);
			}
		} else if (symbol.equals(val))
			return new Node(skiped, stack);
		return findArgIndexs(lambdaArgs.rest(), symbol, skiped + 1, stack);
	}

	private static boolean isJavaCallSymbol(final String op) {
		return op.charAt(0) == '.' || 0 < op.indexOf('/');
	}
	
	private static boolean isJavaClassPathSymbol(final String op) {
		return Character.isUpperCase(op.charAt(0)) ||
				op.charAt(0) != '.' && op.indexOf('.') != -1;
	}

	public static Serializable expand(Serializable instrNodes) {
		//make sure only Instruction List appear in runtime
		return (Serializable) ((Node) instrNodes).toList(Instruction.class);
	}
	
	private static PersistentList compileArgs(PersistentList args, PersistentList lambdaArgs, int startIndex) {
		if (args.isEndingNode()) return BasicType.NIL;
		return listInstruction(
				compile(args.head(), lambdaArgs, startIndex),
				compileArgs(args.rest(), lambdaArgs, startIndex));
	}

	private static Serializable compileCond(PersistentList pairList, PersistentList lambdaArgs, int startIndex) {
		if (pairList.isEndingNode()) return listInstruction(InstructionSetEnum.ldc.create(BasicType.NIL));
		
		Node headPair = (Node) pairList.head();
		
		Serializable condition = compile(headPair.head(), lambdaArgs, startIndex);
		int condInstCount = countInstruction(condition) + 1;
		
		Serializable exp = compile(headPair.second(), lambdaArgs, startIndex + condInstCount);
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
	
	private static PersistentList listInstruction(Object... ins) { return listInstructionRecur(0, ins); }

	private static PersistentList listInstructionRecur(int skipParams, Object... ins) {
		if (ins.length == skipParams) return BasicType.NIL;
		Object s = ins[skipParams];
		if (s instanceof Node)
			return ListUtils.append((PersistentList) s, listInstructionRecur(skipParams + 1, ins));
		else
			return new Node(s, listInstructionRecur(skipParams + 1, ins));
	}
}
