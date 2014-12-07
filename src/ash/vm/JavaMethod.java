package ash.vm;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import ash.lang.BasicType;
import ash.lang.ListUtils;
import ash.lang.PersistentMap;
import ash.lang.PersistentSet;
import ash.lang.Symbol;
import ash.util.JavaUtils;

public final class JavaMethod implements Serializable {
	private static final long serialVersionUID = -933603269059202413L;
	private static final Map<String, JavaMethod> CACHE = new WeakHashMap<>();
	
	private String methodFullName;
	private Class<?> clazz;
	transient private List<Method> candidateMethods;

	public static JavaMethod create(Symbol symbol) {
		String methodName = symbol.name;
		
		JavaMethod javaMethod = CACHE.get(methodName);
		if (javaMethod == null) {
			javaMethod = new JavaMethod(symbol);
			CACHE.put(methodName, javaMethod);
		}
		return javaMethod;
	}

	private JavaMethod(Symbol symbol) {
		methodFullName = symbol.name;
		if (methodFullName.charAt(0) == '.') return;
		
		try {
			clazz = loadClassBySymbol(symbol);
			loadCandidateMethod();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static Class<?> loadClassBySymbol(Symbol symbolName) throws ClassNotFoundException {
		return Class.forName(getFullClassPath(symbolName.name));
	}

	private static String getFullClassPath(String classpath) {
		int sepPos = classpath.indexOf('/');
		String path = sepPos != -1 ? classpath.substring(0, sepPos) : classpath;
		if (Character.isUpperCase(path.charAt(0)) && path.indexOf('.') == -1) {
			return "java.lang." + path;
		}
		return path;
	}
	
	@Override
	public String toString() { return methodFullName; }

	private List<Method> loadCandidateMethod() {
		if (clazz != null && candidateMethods == null) {
			candidateMethods = filterMethod(clazz.getMethods(), getStaticMemberName());
		}
		return candidateMethods;
	}

	private static List<Method> filterMethod(Method[] methods, final String methodName) {
        return Arrays.asList(methods).stream().filter(m -> m.getName().equals(methodName)).collect(Collectors.toList());
	}

	private String getStaticMemberName() {
		return methodFullName.substring(methodFullName.indexOf('/') + 1);
	}

	public Object call(Object[] args) {
		List<Method> candidateMethods = loadCandidateMethod();
		Object rst = candidateMethods != null && 0 < candidateMethods.size()
				? callReflectMethod(args, candidateMethods)
				: callCustomMethod(args);
		if (rst == null) return BasicType.NIL;
		else if (rst instanceof Boolean)
			return ListUtils.transformBoolean(((Boolean) rst).booleanValue());
		return rst;
	}

	private Object callCustomMethod(Object[] args) {
		switch (methodFullName) {
		case ".new":
			return reflectCreateObject(args);
		default:
			if (methodFullName.startsWith(".$")) {
				return readField(methodFullName.substring(2), args);
			} else if (methodFullName.charAt(0) == '.') {
				return callInstanceMethod(methodFullName.substring(1), args);
			}
			throw new UnsupportedOperationException("Unsupport Custom Method Call:" + methodFullName);
		}
	}

	private static Object callReflectMethod(Object[] args, List<Method> candidateMethods) {
		try {
			if (candidateMethods.size() == 1) {
				return candidateMethods.get(0).invoke(null, args);
			} else {
				return matchMethod(candidateMethods, getParameterTypes(args)).invoke(null, args);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when callReflectMethod: " + candidateMethods.get(0).getName()
					+ " with args: " + JavaUtils.displayArray(args, " "), e);
		}
	}

	private static Object callInstanceMethod(String methodName, Object[] args) {
		try {
			Object[] argsArray = subArray(args, 1);
			Method[] methods = args[0].getClass().getMethods();
			Method matchMethod = matchMethod(filterMethod(methods, methodName), getParameterTypes(argsArray));
			matchMethod.setAccessible(true);
			return matchMethod.invoke(args[0], argsArray);
		} catch (Exception e) {
			throw new RuntimeException("Error when callInstanceMethod " + methodName +
					" with args: " + JavaUtils.displayArray(args, " "), e);
		}
	}

	private static Object readField(String fieldName, Object[] args) {
		try {
			if (args[0] instanceof Symbol) {
				return loadClassBySymbol((Symbol) args[0]).getField(fieldName).get(null);
			} else {
				return args[0].getClass().getField(fieldName).get(args[0]);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean instanceOf(Class<?> valClass, Class<?> clazz) {
		return clazz == valClass || clazz.isAssignableFrom(valClass);
	}

	private static Object reflectCreateObject(Object[] args) {
		try {
			Class<?> clazz = loadClassBySymbol((Symbol) args[0]);
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
        return candidateMethods.stream()
                .filter(m -> strictMatch(m.getParameterTypes(), targetParameterTypes)).findFirst()
                .orElseGet(() -> candidateMethods.stream()
                        .filter(m -> fuzzyMatch(m.getParameterTypes(), targetParameterTypes))
                        .findFirst().orElse(null));
	}

	private static Constructor<?> matchConstructor(List<Constructor<?>> candidateConstructors, final Class<?>[] targetParameterTypes) {
        return candidateConstructors.stream()
                .filter(c -> strictMatch(c.getParameterTypes(), targetParameterTypes))
                .findFirst().orElseGet(() -> candidateConstructors.stream()
                        .filter(c -> fuzzyMatch(c.getParameterTypes(), targetParameterTypes))
                        .findFirst().orElse(null));
	}
	
	private static Class<?>[] getParameterTypes(Object[] args) {
        return Arrays.asList(args).stream().map(Object::getClass).toArray(Class[]::new);
	}

	// int match to Integer ignore Float
	private static boolean strictMatch(Class<?>[] methodParameterTypes, Class<?>[] targetParameterTypes) {
		if (methodParameterTypes.length != targetParameterTypes.length) return false;
		for (int i = 0; i < targetParameterTypes.length; i++) {
			if (methodParameterTypes[i].isPrimitive()
                    && STRICT_PRIMITIVE_CLASS_MAP.get(methodParameterTypes[i]) == targetParameterTypes[i]) {
            } else if (instanceOf(targetParameterTypes[i], methodParameterTypes[i])) {
            } else {
				return false;
			}
		}
		return true;
	}
	
	private static boolean fuzzyMatch(Class<?>[] methodParameterTypes, Class<?>[] targetParameterTypes) {
		if (methodParameterTypes.length != targetParameterTypes.length) return false;
		for (int i = 0; i < targetParameterTypes.length; i++) {
			if (methodParameterTypes[i].isPrimitive()
					&& PRIMITIVE_CLASS_MAP.get(methodParameterTypes[i]).contains(targetParameterTypes[i])) {
			} else if (instanceOf(targetParameterTypes[i], methodParameterTypes[i])) {
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
