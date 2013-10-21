package ash.vm;

public final class Maths {
	
	public static Number add(Number p1, Number p2) {
		double d = ((Number) p1).doubleValue() + ((Number) p2).doubleValue();
		int i = (int)d;
		if (d == i) return i;
		return d;
	}

	public static Number subtract(Number p1, Number p2) {
		double d = ((Number) p1).doubleValue() - ((Number) p2).doubleValue();
		int i = (int)d;
		if (d == i) return i;
		return d;
	}

	public static Number multiply(Number p1, Number p2) {
		if (p1 instanceof Integer && p2 instanceof Integer)
			return ((Integer) p1).intValue() * ((Integer) p2).intValue();
		else
			return (((Number) p1).doubleValue() * ((Number) p2).doubleValue());
	}

	public static Number divide(Number p1, Number p2) {
		double res = (((Number) p1).doubleValue() / ((Number) p2).doubleValue());
		if (res == (int) res)
			return (int) res;
		else
			return res;
	}

	public static Number modulus(Number p1, Number p2) {
		if (p1 instanceof Integer && p2 instanceof Integer)
			return ((Integer) p1).intValue() % ((Integer) p2).intValue();
		else
			return (((Number) p1).doubleValue() % ((Number) p2).doubleValue());
	}

	public static boolean greaterThan(Number p1, Number p2) {
		if (p1 instanceof Integer && p2 instanceof Integer)
			return ((Integer) p1).intValue() > ((Integer) p2).intValue();
		else
			return (((Number) p1).doubleValue() > ((Number) p2).doubleValue());
	}
	
	public static boolean greaterEqual(Number p1, Number p2) {
		if (p1 instanceof Integer && p2 instanceof Integer)
			return ((Integer) p1).intValue() >= ((Integer) p2).intValue();
		else
			return (((Number) p1).doubleValue() >= ((Number) p2).doubleValue());
	}
	
	public static boolean lessThan(Number p1, Number p2) {
		if (p1 instanceof Integer && p2 instanceof Integer)
			return ((Integer) p1).intValue() < ((Integer) p2).intValue();
		else
			return (((Number) p1).doubleValue() < ((Number) p2).doubleValue());
	}
	
	public static boolean lessEqual(Number p1, Number p2) {
		if (p1 instanceof Integer && p2 instanceof Integer)
			return ((Integer) p1).intValue() <= ((Integer) p2).intValue();
		else
			return (((Number) p1).doubleValue() <= ((Number) p2).doubleValue());
	}
	
}
