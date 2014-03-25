package ash.parser;

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
		return toTree(ListUtils.reverse(tokenize(src.trim())), BasicType.NIL);
	}
	
	private static final Symbol smallLeft = Symbol.create("("),
								smallRight = Symbol.create(")");
	
	private static final PersistentMap<Symbol, Symbol> QUOTE_MAP = new PersistentMap<>(
			Symbol.create("'"), Symbol.create("quote"),
			Symbol.create("`"), Symbol.create("syntax-quote"),
			Symbol.create("~"), Symbol.create("unquote"),
			Symbol.create("@"), Symbol.create("unquote-splicing"),
			Symbol.create("#"), Symbol.create("regex"));
	
	private static PersistentList toTree(PersistentList reversedTokens, Node base) {
		if (reversedTokens.isEndingNode()) return base;
		Object head = reversedTokens.head();
		if (smallRight.equals(head)) { // )
			PersistentList treeAndRest = toTree(reversedTokens.rest(), BasicType.NIL);
			PersistentList tree = (PersistentList) treeAndRest.head();
			PersistentList theRest = treeAndRest.rest();
			return toTree(theRest, new Node(tree, base));
		} else if (smallLeft.equals(head)) { // (
			return new Node(base, reversedTokens.rest());
		} else if (head instanceof Symbol && QUOTE_MAP.containsKey((Symbol) head)) {
			Symbol quoteSym = QUOTE_MAP.get((Symbol) head);
			Node quoted = new Node(quoteSym, new Node(base.head()));
			return toTree(reversedTokens.rest(), new Node(quoted, base.rest()));
		} else {
			return toTree(reversedTokens.rest(), new Node(BasicType.realType((String) head), base));
		}
	}

	private static final PersistentSet<Character> TOKEN_CHAR_SET = new PersistentSet<>(
			'(', ')', '\'', '`', '~', '@', '#');
	private static final PersistentMap<Character, String> CHAR_TRANSFROM_MAP = new PersistentMap<>(
			'[', "(vector ",
			'{', "(hash-map ",
			']', ")",
			'}', ")");
	
	private static PersistentList tokenize(String src) {
		if (src.isEmpty()) return BasicType.NIL;
		char headChar = src.charAt(0);
		if (Character.isWhitespace(headChar)) {
			return tokenize(src.substring(1));
		} else if (TOKEN_CHAR_SET.contains(headChar)) {
			return new Node(Symbol.create(String.valueOf(headChar)), tokenize(src.substring(1)));
		} else if (CHAR_TRANSFROM_MAP.containsKey(headChar)) {
			return tokenize(CHAR_TRANSFROM_MAP.get(headChar) + src.substring(1));
		} else if (headChar == '$' && 2 < src.length() && '{' == src.charAt(1)) {
			return tokenize("(hash-set " + src.substring(2));
		} else {
			String headText = getHeadElem(src);
			return new Node(headText, tokenize(src.substring(headText.length())));
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
