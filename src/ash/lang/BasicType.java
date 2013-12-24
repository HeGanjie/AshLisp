package ash.lang;

import java.util.regex.Pattern;

public class BasicType {
	public static final String T = "t";
	public static final Node NIL = new Node(null, null);
	private static final Pattern NUMBER_PATTERN = Pattern.compile("[+-]?\\d+");
	private static final Pattern DECIMAL_PATTERN = Pattern.compile("[+-]?(?:\\d+)?\\.\\d+");
	
	public static Object realType(String val) {
		if (NUMBER_PATTERN.matcher(val).matches())
			return Integer.parseInt(val);
		else if (DECIMAL_PATTERN.matcher(val).matches())
			return Double.parseDouble(val);
		else if (val.charAt(0) == '\\') {
			return parseChar(val);
		}
		return val;
	}

	private static char parseChar(String val) {
		if (val.length() == 2)
			return new Character(val.charAt(1));
		else if (val.equals("\\space"))
			return new Character(' ');
		else if (val.equals("\\newline"))
			return new Character('\n');
		throw new IllegalArgumentException("Invalid Char:" + val);
	}
	
	public static Object asString(Object val) {
		if (val instanceof Character) {
			if ((Character)val == ' ')
				return "\\space";
			else if ((Character)val == '\n')
				return "\\newline";
			else
				return '\\' + val.toString();
		}
		return val;
	}
}
