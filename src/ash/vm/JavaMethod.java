package ash.vm;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ash.compiler.Compiler;
import ash.lang.BasicType;
import ash.lang.CharNode;
import ash.lang.ListUtils;
import ash.lang.MacroExpander;
import ash.lang.Node;
import ash.lang.PersistentList;
import ash.lang.PersistentMap;
import ash.lang.PersistentSet;
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

	public static JavaMethod create(Symbol symbol) {
		String methodName = symbol.name;
		
		JavaMethod javaMethod = CACHE.get(methodName);
		if (javaMethod == null) {
			javaMethod = new JavaMethod(methodName);
			CACHE.put(methodName, javaMethod);
		}
		return javaMethod;
	}

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
	
	@Override
	public String toString() { return methodFullName; }

	private List<Method> loadCandidateMethod() {
		if (clazz != null && candidateMethods == null) {
			final String methodName = getStaticMemberName();
			candidateMethods = filterMethod(clazz.getMethods() ,methodName);
		}
		return candidateMethods;
	}

	private static List<Method> filterMethod(Method[] methods, final String methodName) {
		return LambdaUtils.where(Arrays.asList(methods), new Func1<Boolean, Method>() {
			@Override
			public Boolean call(Method method) {
				return method.getName().equals(methodName);
			}
		});
	}

	private String getStaticMemberName() {
		return methodFullName.substring(methodFullName.indexOf('/') + 1);
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

	@SuppressWarnings("unchecked")
	private Object callCustomMethod(Object[] args) {
		switch (methodFullName.substring(1)) {
		case "":
			return callInstanceMethod(args);
		case "instance?":
			return checkInstanceOf(args);
		case "new":
			return reflectCreateObject(args);
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

	private static Object callInstanceMethod(Object[] args) {
		try {
			Object[] subArray = subArray(args, 2);
			Method[] methods = args[0].getClass().getMethods();
			return matchMethod(filterMethod(methods, args[1].toString()), getParameterTypes(subArray))
					.invoke(args[0], subArray);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Object checkInstanceOf(Object[] args) {
		try {
			Class<?> clazz = Class.forName(args[0].toString());
			Class<?> valClass = args[1].getClass();
			return ListUtils.transformBoolean(clazz == valClass || clazz.isAssignableFrom(valClass));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static Object reflectCreateObject(Object[] args) {
		try {
			Class<?> clazz = Class.forName(args[0].toString());
			Object[] newArgs = subArray(args, 1);
			Constructor<?>[] constructors = clazz.getConstructors();
			if (constructors.length == 1) {
				return constructors[0].newInstance(newArgs);
			} else {
				return matchConstructor(Arrays.asList(constructors), getParameterTypes(newArgs)).newInstance(newArgs);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Object[] subArray(Object[] args, int startIndex) {
		Object[] newArgs = new Object[args.length - startIndex];
		System.arraycopy(args, startIndex, newArgs, 0, newArgs.length);
		return newArgs;
	}

	private static Method matchMethod(List<Method> candidateMethods, final Class<?>[] targetParameterTypes) {
		Method firstMatch = LambdaUtils.firstOrNull(candidateMethods, new Func1<Boolean, Method>() {
			@Override
			public Boolean call(Method m) {
				return strictMatch(m.getParameterTypes(), targetParameterTypes);
			}
		});
		if (firstMatch != null) return firstMatch;
		return LambdaUtils.firstOrNull(candidateMethods, new Func1<Boolean, Method>() {
			@Override
			public Boolean call(Method m) {
				return fuzzyMatch(m.getParameterTypes(), targetParameterTypes);
			}
		});
	}

	private static Constructor<?> matchConstructor(List<Constructor<?>> candidateConstructors, final Class<?>[] targetParameterTypes) {
		Constructor<?> firstMatch = LambdaUtils.firstOrNull(candidateConstructors, new Func1<Boolean, Constructor<?>>() {
			@Override
			public Boolean call(Constructor<?> m) {
				return strictMatch(m.getParameterTypes(), targetParameterTypes);
			}
		});
		if (firstMatch != null) return firstMatch;
		return LambdaUtils.firstOrNull(candidateConstructors, new Func1<Boolean, Constructor<?>>() {
			@Override
			public Boolean call(Constructor<?> m) {
				return fuzzyMatch(m.getParameterTypes(), targetParameterTypes);
			}
		});
	}
	
	private static Class<?>[] getParameterTypes(Object[] args) {
		return LambdaUtils.select(Arrays.asList(args), new Func1<Class<?>, Object>() {
			@Override
			public Class<?> call(Object val) { return val.getClass(); }
		}).toArray(EMPTY_CLASSES);
	}

	private static boolean strictMatch(Class<?>[] methodParameterTypes, Class<?>[] targetParameterTypes) {
		if (methodParameterTypes.length != targetParameterTypes.length) return false;
		for (int i = 0; i < targetParameterTypes.length; i++) {
			if (methodParameterTypes[i] == targetParameterTypes[i] ||
					STRICT_PRIMITIVE_CLASS_MAP.get(methodParameterTypes[i]) == targetParameterTypes[i]) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}
	
	private static boolean fuzzyMatch(Class<?>[] methodParameterTypes, Class<?>[] targetParameterTypes) {
		if (methodParameterTypes.length != targetParameterTypes.length) return false;
		for (int i = 0; i < targetParameterTypes.length; i++) {
			if (methodParameterTypes[i] == targetParameterTypes[i] ||
					PRIMITIVE_CLASS_MAP.get(methodParameterTypes[i]).contains(targetParameterTypes[i])) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}

	private static final PersistentMap<Class<?>, Class<?>> STRICT_PRIMITIVE_CLASS_MAP = new PersistentMap<>(
			boolean.class, Boolean.class,
			byte.class, Byte.class,
			char.class, Character.class,
			short.class, Short.class,
			int.class, Integer.class,
			long.class, Long.class,
			float.class, Float.class,
			double.class, Double.class
		);
	
	private static final PersistentMap<Class<?>, PersistentSet<Class<?>>> PRIMITIVE_CLASS_MAP = new PersistentMap<>(
		boolean.class, new PersistentSet<>(Boolean.class),
		byte.class, new PersistentSet<>(Byte.class, Character.class),
		char.class, new PersistentSet<>(Byte.class, Character.class),
		short.class, new PersistentSet<>(Byte.class, Character.class, Short.class),
		int.class, new PersistentSet<>(Byte.class, Character.class, Short.class, Integer.class),
		long.class, new PersistentSet<>(Byte.class, Character.class, Short.class, Integer.class, Long.class),
		float.class, new PersistentSet<>(Byte.class, Character.class, Short.class, Integer.class, Float.class),
		double.class, new PersistentSet<>(Byte.class, Character.class, Short.class, Integer.class, Float.class, Double.class)
	);
}
