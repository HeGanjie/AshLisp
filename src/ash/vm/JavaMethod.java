package ash.vm;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ash.compiler.Compiler;
import ash.lang.BasicType;
import ash.lang.CharNode;
import ash.lang.LazyNode;
import ash.lang.ListUtils;
import ash.lang.MacroExpander;
import ash.lang.Node;
import ash.lang.PersistentList;
import ash.lang.PersistentMap;
import ash.lang.Symbol;
import ash.parser.Parser;
import bruce.common.functional.Func1;
import bruce.common.functional.LambdaUtils;
import bruce.common.utils.CommonUtils;

public final class JavaMethod implements Serializable {
	private static final Class<?>[] EMPTY_CLASSES = new Class<?>[]{};
	private static final long serialVersionUID = -933603269059202413L;
	private static final Map<String, JavaMethod> CACHE = new HashMap<>();
	
	private String methodFullName;
	private Class<?> clazz;
	transient private List<Method> candidateMethods;

	private JavaMethod(String name) {
		methodFullName = name;
		if (name.charAt(0) == '.') return;
		
		try {
			clazz = Class.forName(methodFullName.substring(0, methodFullName.indexOf('/')));
			loadCandidateMethod();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private List<Method> loadCandidateMethod() {
		if (clazz != null && candidateMethods == null) {
			final String methodName = getStaticMethodName();
			candidateMethods = LambdaUtils.where(Arrays.asList(clazz.getMethods()), new Func1<Boolean, Method>() {
				@Override
				public Boolean call(Method method) {
					return method.getName().equals(methodName);
				}
			});
		}
		return candidateMethods;
	}

	public Object call(Object[] args) {
		List<Method> candidateMethods = loadCandidateMethod();
		if (candidateMethods != null && 0 < candidateMethods.size()) {
			try {
				if (candidateMethods.size() == 1) {
					return candidateMethods.get(0).invoke(null, args);
				} else {
					return matchMethod(candidateMethods, getParameterTypes(args)).invoke(null, args);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return callCustomMethod(args);
	}

	private Method matchMethod(List<Method> candidateMethods, final Class<?>[] targetParameterTypes) {
		return LambdaUtils.firstOrNull(candidateMethods, new Func1<Boolean, Method>() {
			@Override
			public Boolean call(Method m) {
				return allMatch(m.getParameterTypes(), targetParameterTypes);
			}
		});
	}
	
	private static final PersistentMap<Class<?>, Class<?>> PRIMITIVE_CLASS_MAP = new PersistentMap<>(new HashMap<Class<?>, Class<?>>(){
		private static final long serialVersionUID = -6095060982667447960L;

	{
		put(boolean.class, Boolean.class);
		put(byte.class, Byte.class);
		put(short.class, Short.class);
		put(int.class, Integer.class);
		put(long.class, Long.class);
		put(float.class, Float.class);
		put(double.class, Double.class);
	}});
	
	private static final PersistentMap<Class<?>, Class<?>> BOX_CLASS_MAP = new PersistentMap<>(new HashMap<Class<?>, Class<?>>(){
		private static final long serialVersionUID = 1198282101259361358L;

	{
		put(Boolean.class, boolean.class);
		put(Byte.class, byte.class);
		put(Short.class, short.class);
		put(Integer.class, int.class);
		put(Long.class, long.class);
		put(Float.class, float.class);
		put(Double.class, double.class);
	}});

	private boolean allMatch(Class<?>[] methodParameterTypes, Class<?>[] targetParameterTypes) {
		for (int i = 0; i < targetParameterTypes.length; i++) {
			if (methodParameterTypes[0] == targetParameterTypes[0]) {
				continue;
			} else if (PRIMITIVE_CLASS_MAP.get(methodParameterTypes[0]) == targetParameterTypes[0] ||
					BOX_CLASS_MAP.get(methodParameterTypes[0]) == targetParameterTypes[0]) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}
	

	@SuppressWarnings("unchecked")
	private Object callCustomMethod(Object[] args) {
		switch (methodFullName.substring(1)) {
		case "lazy-seq":
			return new LazyNode((Closure) args[0]);
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
							: ListUtils.toSeq(((Iterable<PersistentList>) args[0]).iterator());
		case "parse":
			return Parser.split((String) args[0]);
		case "compile":
			return Compiler.astsToInsts(new Node(args[0]));
		case "vmexec":
			return new VM().runInMain((Node) args[0]);
		case "regex":
			return Pattern.compile((String) args[0]);
		case "new-macro":
			MacroExpander.MARCOS_MAP.put((Symbol) args[0], (Node)args[1]);
			break;
		case "expand-macro":
			return MacroExpander.expand((Node) args[0]);
		default:
			throw new UnsupportedOperationException("Unsupport Java Call:" + methodFullName);
		}
		return BasicType.NIL;
	}

	private Class<?>[] getParameterTypes(Object[] args) {
		return LambdaUtils.select(Arrays.asList(args), new Func1<Class<?>, Object>() {
			@Override
			public Class<?> call(Object val) {
				return val.getClass();
			}
		}).toArray(EMPTY_CLASSES);
	}

	private String getStaticMethodName() {
		return methodFullName.substring(methodFullName.indexOf('/') + 1);
	}

	public static JavaMethod create(Symbol symbol) {
		String methodName = symbol.name;
		
		JavaMethod javaMethod = CACHE.get(methodName);
		if (javaMethod == null) {
			javaMethod = new JavaMethod(methodName);
			CACHE.put(methodName, javaMethod);
		}
		return javaMethod;
	}

	@Override
	public String toString() { return '.' + methodFullName; }

}
