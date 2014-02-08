package ash.compiler;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ash.lang.BasicType;
import ash.lang.ListUtils;
import ash.lang.MacroExpander;
import ash.lang.Node;
import ash.lang.PersistentList;
import ash.lang.Symbol;
import ash.vm.ClosureArgs;
import ash.vm.Instruction;
import ash.vm.InstructionSet;
import ash.vm.JavaMethod;
import bruce.common.functional.Func1;

public final class Compiler {
	public static final Symbol MULTI_ARGS_SIGNAL = Symbol.create(".");
	private static final Set<String> NORMAL_INSTRUCTION_SET = new HashSet<>(Arrays.asList(
			"def", "quote", "cond", "lambda*",
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

	private static Serializable compile(final Object exp, Node lambdaContext, int startIndex) {
		if (exp instanceof Node)
			return compileNode((Node) exp, lambdaContext, startIndex);
		else
			return compileObject(exp, lambdaContext);
	}

	private static Serializable compileObject(final Object exp, PersistentList lambdaContext) {
		if (exp instanceof Symbol)
			return listInstruction(compileSymbol((Symbol) exp, lambdaContext)); // (... a add puts ...)
		else
			return listInstruction(InstructionSet.ldc.create(exp)); // (... 1 2 3.4 \a "b" closure ...)
	}

	private static Serializable compileNode(Node node, Node lambdaContext, int startIndex) {
		final Object op = node.head(); // (operation ...)
		if (op instanceof Symbol && NORMAL_INSTRUCTION_SET.contains(((Symbol) op).name)) {
			switch (op.toString()) {
			case "def":
				return listInstruction(
						compile(node.third(), lambdaContext, startIndex),
						InstructionSet.asn.create(node.second()));
			case "quote":
				return listInstruction(InstructionSet.ldc.create(node.second()));
			case "cond":
				return compileCond(node.rest(), lambdaContext, startIndex);
			case "lambda*":
				return compileLambdaLazy(node, lambdaContext);
			default:
				return compileInstCall((Symbol) op, node.rest(), lambdaContext, startIndex);
			}
		} else if (MacroExpander.SYNTAX_QUOTE.equals(op)) {// `(...)
			return compile(MacroExpander.visitSyntaxQuote(node.second()), lambdaContext, startIndex);
		} else if (op instanceof Symbol && MacroExpander.hasMacro(node)) { // (let ...)
			return compile(MacroExpander.expand(node), lambdaContext, startIndex);
		} else { // (func ...) | (.new ...) | ((lambda ...) ...) | (closure ...)
			return compileCall(op, node.rest(), lambdaContext, startIndex);
		}
	}

	private static Serializable compileLambdaLazy(Node lambdaBody, Node lambdaContext) {
		return listInstruction(
				InstructionSet.closure.create(new ClosureArgs(lambdaBody, lambdaContext)));
	}
	
	public static List<Instruction> compileLambda(Node lambdaBody, Node lambdaContext) {
		PersistentList paramList = (PersistentList) lambdaBody.second();
		int dotIndex = ListUtils.indexOf(paramList, MULTI_ARGS_SIGNAL, 0);
		boolean notCombineArgs = dotIndex == -1;
		PersistentList argsList = notCombineArgs ? paramList : removeDot(paramList);
		return expand(listInstructionRecur(notCombineArgs ? 1 : 0,
				InstructionSet.cons_args.create(dotIndex),
				compile(lambdaBody.third(),
						(Node) ListUtils.append(argsList, lambdaContext),
						notCombineArgs ? 0 : 1),
						InstructionSet.ret.create()));
	}

	private static Serializable compileCall(Object op, PersistentList argList, Node lambdaContext, int startIndex) {
		int argsCount = ListUtils.count(argList);
		InstructionSet callMethod = op instanceof Symbol && isJavaCallSymbol(((Symbol) op).name)
				? InstructionSet.java_call
				: InstructionSet.call;
		return listInstruction(
				compileArgs(argList, lambdaContext, startIndex),
				compile(op, lambdaContext, startIndex),
				callMethod.create(argsCount));
	}

	private static PersistentList compileInstCall(Symbol inst, PersistentList argList, Node lambdaContext, int startIndex) {
		return listInstruction(
				compileArgs(argList, lambdaContext, startIndex),
				InstructionSet.valueOf(inst.name).create());
	}

	private static PersistentList removeDot(PersistentList seq) {
		return ListUtils.filter(seq, new Func1<Boolean, Object>() {
			@Override
			public Boolean call(Object symbol) { return !MULTI_ARGS_SIGNAL.equals(symbol); }
		});
	}

	private static Serializable compileSymbol(final Symbol symbol, PersistentList lambdaContext) {
		String methodName = symbol.name;
		if (isJavaCallSymbol(methodName))
			return InstructionSet.ldc.create(JavaMethod.create(symbol)); // java method
		else if (isJavaClassPathSymbol(methodName))
			return InstructionSet.ldc.create(symbol); // java class path

		PersistentList argIndexs = findArgIndexs(lambdaContext, symbol, 0, BasicType.NIL);
		if (argIndexs.isEndingNode()) {
			if (InstructionSet.contains(methodName))
				return InstructionSet.ldc.create(InstructionSet.valueOf(methodName).create()); // instruction
			else
				return InstructionSet.ldv.create(symbol); // symbol
		} else if (argIndexs.rest().isEndingNode())
			return InstructionSet.ldp.create(argIndexs.head()); // symbol index of params
		else
			return InstructionSet.ldt.create(argIndexs); // destructuring
	}

	private static PersistentList findArgIndexs(PersistentList lambdaContext, Symbol symbol, int skiped, Node stack) {
		if (lambdaContext.isEndingNode()) return BasicType.NIL;
		Object val = lambdaContext.head();
		if (val instanceof Node) {
			PersistentList rst = findArgIndexs((PersistentList) val, symbol, 0, new Node(skiped, stack));
			if (!rst.isEndingNode()) return rst;
		} else if (MULTI_ARGS_SIGNAL.equals(val)) {
			if (symbol.equals(lambdaContext.second())) {
				return new Node(-skiped, stack);
			}
		} else if (symbol.equals(val))
			return new Node(skiped, stack);
		return findArgIndexs(lambdaContext.rest(), symbol, skiped + 1, stack);
	}

	private static boolean isJavaCallSymbol(final String op) {
		return op.charAt(0) == '.' || 0 < op.indexOf('/');
	}
	
	private static boolean isJavaClassPathSymbol(final String op) {
		return Character.isUpperCase(op.charAt(0)) ||
				op.charAt(0) != '.' && op.indexOf('.') != -1;
	}

	private static List<Instruction> expand(Serializable instrNodes) {
		//make sure only Instruction List appear in runtime
		return ((Node) instrNodes).toList(Instruction.class);
	}
	
	private static PersistentList compileArgs(PersistentList args, Node lambdaContext, int startIndex) {
		if (args.isEndingNode()) return BasicType.NIL;
		return listInstruction(
				compile(args.head(), lambdaContext, startIndex),
				compileArgs(args.rest(), lambdaContext, startIndex));
	}

	private static Serializable compileCond(PersistentList pairList, Node lambdaContext, int startIndex) {
		if (pairList.isEndingNode()) return listInstruction(InstructionSet.ldc.create(BasicType.NIL));
		
		Node headPair = (Node) pairList.head();
		
		Serializable condition = compile(headPair.head(), lambdaContext, startIndex);
		int condInstCount = countInstruction(condition) + 1;
		
		Serializable exp = compile(headPair.second(), lambdaContext, startIndex + condInstCount);
		int nextCaseStartIndex = startIndex + condInstCount + countInstruction(exp) + 1;
		
		Serializable nextCase = compileCond(pairList.rest(), lambdaContext, nextCaseStartIndex);
		
		return listInstruction(
				condition,
				InstructionSet.jz.create(nextCaseStartIndex),
				exp,
				InstructionSet.jmp.create(nextCaseStartIndex + countInstruction(nextCase)),
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
