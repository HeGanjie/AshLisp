package ash.parser;

import ash.lang.*;

import java.util.function.BiFunction;

public final class Parser {
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

    private static PersistentList toTree(PersistentList reversedTokens, Node base, BiFunction<PersistentList, PersistentList, PersistentList> continuation) {
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

    private static final PersistentSet<Character> NO_SYMBOL_CHAR_SET = new PersistentSet<>('(', ')', '[', ']', '{', '}');
	private static final PersistentSet<Character> META_CHAR_SET = NO_SYMBOL_CHAR_SET.merge(QUOTE_CHAR_MAP.keySet());
	private static final PersistentMap<Character, PersistentList> CHAR_TRANSFORM_MAP = new PersistentMap<>(
			'[', ListUtils.toSeq(0, '(', "vector"),
			']', new Node(')'),
            '{', ListUtils.toSeq(0, '(', "hash-map"),
			'}', new Node(')'));

    private static final PersistentList HASH_SET_HEAD_TOKEN = ListUtils.toSeq(0, '(', "hash-set");
    private static PersistentList tokenize(String src, int offset) {
		if (src.length() == offset) return BasicType.NIL;
		char headCh = src.charAt(offset);
		if (Character.isWhitespace(headCh)) {
			return tokenize(src, offset + 1);
        } else if (META_CHAR_SET.contains(headCh)) {
            if (CHAR_TRANSFORM_MAP.containsKey(headCh))
                return ListUtils.append(CHAR_TRANSFORM_MAP.get(headCh), tokenize(src, offset + 1));
            else
                return new Node(headCh, tokenize(src, offset + 1));
		} else if (headCh == '$' && offset + 1 < src.length() && src.charAt(offset + 1) == '{') {
            return ListUtils.append(HASH_SET_HEAD_TOKEN, tokenize(src, offset + 2));
		} else {
            String headText = getHeadElem(src, offset);
			return new Node(headText, tokenize(src, offset + headText.length()));
		}
	}

    private static final char STRING_WRAPPING_CHAR = '\"';
	private static String getHeadElem(String str, int offset) {
		char headCh = str.charAt(offset);
		return headCh == STRING_WRAPPING_CHAR
				? str.substring(offset, getStringElemLen(str, offset, offset, '\0'))
				: getHeadSymbol(str, offset, offset);
	}

	private static String getHeadSymbol(String str, int start, int end) {
        if (str.length() == end) return str.substring(start);
        char headCh = str.charAt(end);
        if (Character.isWhitespace(headCh) || NO_SYMBOL_CHAR_SET.contains(headCh))
            return str.substring(start, end);
        else
            return getHeadSymbol(str, start, end + 1);
    }

    private static final char ESCAPE_CHAR = '\\';
	private static int getStringElemLen(String src, final int start, int elemLen, char flag) {
		if (elemLen != start && flag == '\0') return elemLen;
		
		final char c = src.charAt(elemLen);
		return getStringElemLen(src, start,
                elemLen + (c == ESCAPE_CHAR ? 2 : 1),
                STRING_WRAPPING_CHAR == c ? (flag == c ? '\0' : c) : flag);
	}
}
