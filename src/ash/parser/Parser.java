package ash.parser;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ash.lang.BasicType;
import ash.lang.Node;
import ash.lang.PersistentList;

public final class Parser {
	private static final Pattern getFirstPlainTextPattern = Pattern.compile("(\\S+)\\s*");
	private static final char ESCAPE_CHAR = '\\';
	private static final char STRING_WRAPPING_CHAR = '\"';
	private static final char QUOTE_CHAR = '\'';

	private Parser() {}
	
	private static Serializable createAst(String readIn) {
		return readIn.charAt(0) == '(' ? split(unwrap(readIn)) : readIn;
	}

	private static String unwrap(String exp) {
		if (exp.charAt(0) == '(' && exp.charAt(exp.length() - 1) == ')')
			return exp.substring(1, exp.length() - 1);
		throw new UnsupportedOperationException("Can not Unwrap:" + exp);
	}

	public static PersistentList split(String str) {
		String trim = str.trim();
		if (trim.length() == 0) return BasicType.NIL;
		
		if (trim.charAt(0) == QUOTE_CHAR) {
			String first = getFirst(trim.substring(1));
			String rest = getRest(trim, first.length() + 1);
			return new Node(new Node("quote", split(first)), split(rest));
		} else {
			String first = getFirst(trim);
			String rest = getRest(trim, first.length());
			return new Node(createAst(first), split(rest));
		}
	}

	private static String getRest(String str, int firstStrLen) {
		return str.substring(firstStrLen);
	}

	private static String getFirst(String str) {
		char headCh = str.charAt(0);
		return headCh == '(' || headCh == STRING_WRAPPING_CHAR
				? str.substring(0, getFirstElemLen(str, 0, 0, '\0'))
				: getHeadPlainText(str);
	}

	private static String getHeadPlainText(String str) {
		Matcher m = getFirstPlainTextPattern.matcher(str);
		m.find();
		return m.group(1);
	}

	private static int getFirstElemLen(String src, int balance, int elemLen, char spanChar) {
		if (elemLen != 0 && balance == 0 && spanChar == '\0') return elemLen;
		
		final char c = src.charAt(elemLen);
		return getFirstElemLen(src,
				spanChar == STRING_WRAPPING_CHAR
					? balance
					: balance + (c == '(' ? 1 : (c == ')' ? -1 : 0)),
				elemLen + (c == ESCAPE_CHAR ? 2 : 1),
				STRING_WRAPPING_CHAR == c ? (spanChar == c ? '\0' : c) : spanChar);
	}
}