package ash.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ash.lang.BasicType;
import ash.lang.Node;
import ash.lang.PersistentList;
import ash.lang.PersistentMap;
import ash.lang.PersistentSet;
import ash.lang.Symbol;

public final class Parser {
	private static final Pattern getFirstPlainTextPattern = Pattern.compile("(\\S+)\\s*");
	private static final PersistentSet<Character> COLLECTION_HEAD_CHAR_SET = new PersistentSet<>( '(', '[', '{');
	private static final PersistentSet<Character> COLLECTION_TAIL_CHAR_SET = new PersistentSet<>( ')', ']', '}');
	private static final char ESCAPE_CHAR = '\\';
	private static final char STRING_WRAPPING_CHAR = '\"';
	private static final PersistentMap<Character, Symbol> QUOTE_CHAR_MAP = new PersistentMap<>(
			'\'', Symbol.create("quote"),
			'`', Symbol.create("syntax-quote"),
			'~', Symbol.create("unquote"),
			'@', Symbol.create("unquote-splicing"),
			'#', Symbol.create("regex"));

	private Parser() {}
	
	private static String unwrap(String exp) {
		return exp.substring(1, exp.length() - 1);
	}

	private static Object createAst(String readIn) {
		char firstChar = readIn.charAt(0);
		if (COLLECTION_HEAD_CHAR_SET.contains(firstChar)) {
			PersistentList splitInner = split(unwrap(readIn));
			if (firstChar == '(') return splitInner;
			else if (firstChar == '[')
				return new Node(Symbol.create("vector"), splitInner);
			else if (firstChar == '{')
				return new Node(Symbol.create("hash-map"), splitInner);
			throw new IllegalArgumentException();
		} else if (firstChar == '$') {
			PersistentList splitInner = split(unwrap(readIn.substring(1)));
			return new Node(Symbol.create("hash-set"), splitInner);
		}
		return BasicType.realType(readIn);
	}

	public static PersistentList split(String str) {
		String trim = str.trim();
		if (trim.length() == 0) return BasicType.NIL;
		
		if (QUOTE_CHAR_MAP.containsKey(trim.charAt(0))) {
			PersistentList innerSplit = split(trim.substring(1));
			Node head = new Node(QUOTE_CHAR_MAP.get(trim.charAt(0)), new Node(innerSplit.head()));
			return new Node(head, innerSplit.rest());
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
		if (headCh == '$')
			return str.substring(0, getFirstElemLen(str.substring(1), 0, 0, '\0') + 1);
		return COLLECTION_HEAD_CHAR_SET.contains(headCh) || headCh == STRING_WRAPPING_CHAR
				? str.substring(0, getFirstElemLen(str, 0, 0, '\0'))
				: getHeadPlainText(str);
	}

	private static String getHeadPlainText(String str) {
		Matcher m = getFirstPlainTextPattern.matcher(str);
		m.find();
		return m.group(1);
	}

	private static int getBalanceDelta(final char c) {
		if (COLLECTION_HEAD_CHAR_SET.contains(c)) return 1;
		else if (COLLECTION_TAIL_CHAR_SET.contains(c)) return -1;
		return 0;
	}

	private static int getFirstElemLen(String src, int balance, int elemLen, char spanChar) {
		if (elemLen != 0 && balance == 0 && spanChar == '\0') return elemLen;
		
		final char c = src.charAt(elemLen);
		return getFirstElemLen(src,
				spanChar == STRING_WRAPPING_CHAR ? balance : balance + getBalanceDelta(c),
				elemLen + (c == ESCAPE_CHAR ? 2 : 1),
				STRING_WRAPPING_CHAR == c ? (spanChar == c ? '\0' : c) : spanChar);
	}
}
