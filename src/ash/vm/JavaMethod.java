package ash.vm;

import java.io.Serializable;

import bruce.common.utils.CommonUtils;

import ash.compiler.Compiler;
import ash.parser.ListUtils;
import ash.parser.Node;

public final class JavaMethod implements Serializable {
	private static final long serialVersionUID = -933603269059202413L;
	String methodName;

	public JavaMethod(String name) { methodName = name; }

	@Override
	public String toString() {
		return CommonUtils.buildString('*', methodName, '*');
	}

	public Serializable call(Serializable[] args) {
		switch (methodName) {
		case "num?":
			return ListUtils.transformBoolean(args[0] instanceof Number);
		case "puts":
			System.out.println(CommonUtils.displayArray(args, ""));
			break;
		case "str":
			return CommonUtils.displayArray(args, "");
		case "compile":
			return Compiler.astsToInsts(new Node(args[0]));
		case "vmexec":
			return new VM().runInMain((Node) args[0]);
		default:
			throw new UnsupportedOperationException("Unsupport Java Call:" + methodName);
		}
		return Node.NIL;
	}

}
