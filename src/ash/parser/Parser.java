package ash.parser;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bruce.downloader.framework.utils.CommonUtils;

public final class Parser {
	private static final Pattern getFirstUnwrappedPattern = Pattern.compile("(\\S+)\\s*");

	protected Serializable createAst(String readIn) {
		if (readIn.charAt(0) == '(') {
			String unWrapped = unWrapped(readIn);
			return CommonUtils.isStringNullOrWriteSpace(unWrapped) ? Node.NIL : split(unWrapped);
		} else
			return readIn;
	}

	private String unWrapped(String exp) {
		if (exp.charAt(0) == '(' && exp.charAt(exp.length() - 1) == ')')
			return exp.substring(1, exp.length() - 1);
		throw new UnsupportedOperationException("Can not Unwrap:" + exp);
	}

	public Node split(String str) {
		String trim = str.trim();
		boolean quoteSugar = trim.charAt(0) == '\'';
		String first = quoteSugar ? getFirst(trim.substring(1)) : getFirst(trim);
		String rest = getRest(trim, quoteSugar ? first.length() + 1 : first.length());
		
		Serializable ast = quoteSugar ? new Node("quote", new Node(createAst(first))) : createAst(first);
		if (CommonUtils.isStringNullOrWriteSpace(rest))
			return new Node(ast);
		else
			return new Node(ast, split(rest));
	}

	private String getRest(String str, int firstStrLen) {
		return str.substring(firstStrLen);
	}

	private String getFirst(String str) {
		return str.charAt(0) == '('
				? str.substring(0, getFirstElemLen(str, 0, 0))
				: getFirstPlainText(str);
	}

	protected String getFirstPlainText(String str) {
		Matcher m = getFirstUnwrappedPattern.matcher(str);
		m.find();
		return m.group(1);
	}
	
	private int getFirstElemLen(String src, int balance, int elemLen) {
		if (elemLen != 0 && balance == 0) return elemLen;
		
		final char c = src.charAt(elemLen);
		return getFirstElemLen(src, balance + (c == '(' ? -1 : (c == ')' ? 1: 0)), elemLen + 1);
	}

}
