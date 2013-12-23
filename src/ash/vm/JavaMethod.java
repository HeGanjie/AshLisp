package ash.vm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import ash.compiler.Compiler;
import ash.lang.BasicType;
import ash.lang.CharNode;
import ash.lang.LazyNode;
import ash.lang.ListUtils;
import ash.lang.MacroExpander;
import ash.lang.Node;
import ash.parser.Parser;
import bruce.common.utils.CommonUtils;

public final class JavaMethod implements Serializable {
	private static final long serialVersionUID = -933603269059202413L;
	private static final Map<String, JavaMethod> CACHE = new HashMap<>();
	
	String methodName;

	private JavaMethod(String name) { methodName = name; }
	
	public static JavaMethod create(String methodName) {
		JavaMethod javaMethod = CACHE.get(methodName);
		if (javaMethod == null) {
			javaMethod = new JavaMethod(methodName);
			CACHE.put(methodName, javaMethod);
		}
		return javaMethod;
	}

	@Override
	public String toString() { return '.' + methodName; }

	@SuppressWarnings("unchecked")
	public Serializable call(Serializable[] args) {
		switch (methodName) {
		case "stream":
			if (args.length == 1)
				return LazyNode.create(args[0], BasicType.NIL);
			return LazyNode.create(args[0], args[1]);
		case "num?":
			return ListUtils.transformBoolean(args[0] instanceof Number);
		case "puts":
			System.out.println(CommonUtils.displayArray(args, ""));
			break;
		case "str":
			return CommonUtils.displayArray(args, "");
		case "seq":
			return args[0] instanceof String
					? CharNode.create((String) args[0])
					: ListUtils.toSeq(((Iterable<Serializable>) args[0]).iterator());
		case "parse":
			return Parser.split((String) args[0]);
		case "compile":
			return Compiler.astsToInsts(new Node(args[0]));
		case "vmexec":
			return new VM().runInMain((Node) args[0]);
		case "regex":
			return Pattern.compile((String) args[0]);
		case "new-macro":
			MacroExpander.MARCOS_MAP.put((String)args[0], (Node)args[1]);
			break;
		case "expand-macro":
			return MacroExpander.expand((Node) args[0]);
		default:
			throw new UnsupportedOperationException("Unsupport Java Call:" + methodName);
		}
		return BasicType.NIL;
	}

}
