package ash.parser;

import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ash.lang.BasicType;
import ash.lang.ListUtils;
import ash.lang.Node;
import ash.lang.PersistentList;
import ash.lang.PersistentMap;
import ash.lang.PersistentSet;
import ash.lang.Symbol;

public final class Parser {
	private static final Pattern getFirstSymbolPattern = Pattern.compile("([^\\s\\(\\)\\[\\]\\{\\}]+)\\s*");
	private static final char ESCAPE_CHAR = '\\';
	private static final char STRING_WRAPPING_CHAR = '\"';

	private Parser() {}
	
	public static PersistentList parse(String src) {
		return toTree(ListUtils.reverse(tokenize(src, 0)), BasicType.NIL, null);
	}
	
	private static final PersistentMap<Character, Symbol> QUOTE_CHAR_MAP = new PersistentMap<>(
			'\'', Symbol.create("quote"),
			'`', Symbol.create("syntax-quote"),
			'~', Symbol.create("unquote"),
			'@', Symbol.create("unquote-splicing"),
			'#', Symbol.create("regex"));
	
	private static PersistentList toTree(PersistentList reversedTokens, Node base, BiFunction<Object, PersistentList, PersistentList> continuation) {
		if (reversedTokens.isEndingNode()) return base;
		Object head = reversedTokens.head();
		if (head.equals(')')) {
            return toTree(reversedTokens.rest(), BasicType.NIL, (node, restToken) -> toTree(restToken, new Node(node, base), continuation));
		} else if (head.equals('(')) {
            return continuation.apply(base, reversedTokens.rest());
		} else if (head instanceof Character) {
			Symbol quoteSym = QUOTE_CHAR_MAP.get((Character) head);
			Node quoted = new Node(quoteSym, new Node(base.head()));
			return toTree(reversedTokens.rest(), new Node(quoted, base.rest()), continuation);
		} else {
			return toTree(reversedTokens.rest(), new Node(BasicType.realType((String) head), base), continuation);
		}
	}

	private static final PersistentSet<Character> META_CHAR_SET = new PersistentSet<>(
			'(', ')', '\'', '`', '~', '@', '#');
	private static final PersistentMap<Character, String> CHAR_TRANSFORM_MAP = new PersistentMap<>(
			'[', "(vector ",
			'{', "(hash-map ",
			']', ")",
			'}', ")");
	
	public static PersistentList tokenize(String src, int offset) {
		if (src.length() == offset) return BasicType.NIL;
		char c = src.charAt(offset);
		if (Character.isWhitespace(c)) {
			return tokenize(src, offset + 1);
        } else if (META_CHAR_SET.contains(c)) {
            return new Node(c, tokenize(src, offset + 1));
        } else if (CHAR_TRANSFORM_MAP.containsKey(c)) {
            return tokenize(CHAR_TRANSFORM_MAP.get(c) + src.substring(offset + 1), 0);
		} else if (c == '$' && offset + 1 < src.length() && src.charAt(offset + 1) == '{') {
			return tokenize("(hash-set " + src.substring(offset + 2), 0);
		} else {
            String s = src.substring(offset);
            String headText = getHeadElem(s);
			return new Node(headText, tokenize(s, headText.length()));
		}
	}
	
	private static String getHeadElem(String str) {
		char headCh = str.charAt(0);
		return headCh == STRING_WRAPPING_CHAR
				? str.substring(0, getStringElemLen(str, 0, '\0'))
				: getHeadSymbol(str);
	}

	private static String getHeadSymbol(String str) {
		Matcher m = getFirstSymbolPattern.matcher(str);
		m.find();
		return m.group(1);
	}

	private static int getStringElemLen(String src, int elemLen, char spanChar) {
		if (elemLen != 0 && spanChar == '\0') return elemLen;
		
		final char c = src.charAt(elemLen);
		return getStringElemLen(src,
				elemLen + (c == ESCAPE_CHAR ? 2 : 1),
				STRING_WRAPPING_CHAR == c ? (spanChar == c ? '\0' : c) : spanChar);
	}
}
