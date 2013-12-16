package ash.lang;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class MacroExpander {
	private static final String MORE_ELEM = "*";
	private static final String PLACE_HERE_ELEM = "%";
	private static final String STACK_POINT_ELEM = "@";
	public static final Map<String, Node> MARCOS_MAP = new HashMap<>();

	private MacroExpander() {}
	
	public static boolean hasMacro(String symbol) {
		return MARCOS_MAP.containsKey(symbol);
	}

	public static Node expand(Node ast) {
		String macroName = (String) ast.left;
		Node pAndt = MARCOS_MAP.get(macroName);
		Node pattern = (Node) pAndt.left;
		Node template = (Node) pAndt.next.left;
		
		PersistentMap<String, Serializable> mapping = findMapping(pattern.next, ast.next);
		Node moreElem = (Node) mapping.get(MORE_ELEM);
		if (moreElem == null)
			return createAstByTemplate(mapping, template, null, null);
		return createAstRecur(mapping.dissoc(MORE_ELEM), template, moreElem, null, countReplacementRequire(template));
	}
	
	private static Node createAstByTemplate(PersistentMap<String, Serializable> mapping, Node template, Node moreElem, Node stack) {
		if (template == Node.NIL) return Node.NIL;
		Serializable leftReplacement = null;
		if (template.left instanceof Node)
			leftReplacement = createAstByTemplate(mapping, (Node) template.left, moreElem, stack);
		else {
			if (template.left instanceof String) {
				String symbol = (String) template.left;
				if (symbol.startsWith(PLACE_HERE_ELEM)) // %
					leftReplacement = getRequiringReplacement(symbol, moreElem);
				else if (symbol.startsWith(STACK_POINT_ELEM)) // @x
					leftReplacement = stack == null ? mapping.get(symbol.substring(1)) : stack;
				else
					leftReplacement = mapping.get(symbol);
			}
			if (leftReplacement == null)
				leftReplacement = template.left;
		}
		return new Node(leftReplacement, createAstByTemplate(mapping, template.next, moreElem, stack));
	}
	
	private static Serializable getRequiringReplacement(String symbol, Node moreElem) {
		if (PLACE_HERE_ELEM.equals(symbol)) {
			return moreElem.left;
		} else {
			int drop = Integer.parseInt(symbol.substring(1)) - 1;
			Serializable replacement = ListUtils.drop(drop, moreElem).left;
			if (replacement == null)
				throw new IllegalArgumentException("Macro Illegal!");
			return replacement;
		}
	}

	private static Node createAstRecur(PersistentMap<String, Serializable> mapping,
			Node template, Node moreElem, Node stack, int wildcardCount) {
		
		if (moreElem != Node.NIL) {
			Node res = createAstByTemplate(mapping, template, ListUtils.take(wildcardCount, moreElem), stack);
			return createAstRecur(mapping, template, ListUtils.drop(wildcardCount, moreElem), res, wildcardCount);
		}
		return stack;
	}

	private static PersistentMap<String, Serializable> findMapping(Node pattern, Node ast) {
		if (pattern == Node.NIL) return new PersistentMap<>();
		
		PersistentMap<String, Serializable> leftMap;
		if (pattern.left instanceof Node)
			leftMap = findMapping((Node) pattern.left, (Node) ast.left);
		else {
			String symbol = (String) pattern.left;
			leftMap = new PersistentMap<>(symbol, MORE_ELEM.equals(symbol) ? ast : ast.left);
		}
		
		return leftMap.merge(findMapping(pattern.next, ast.next));
	}

	private static int countReplacementRequire(Node template) {
		if (template == Node.NIL)
			return 0;
		else if (template.left instanceof Node)
			return countReplacementRequire((Node) template.left) + countReplacementRequire(template.next);
		else {
			Serializable val = template.left;
			if (val instanceof String && ((String) val).startsWith(PLACE_HERE_ELEM))
				return 1 + countReplacementRequire(template.next);
		}
		return countReplacementRequire(template.next);
	}
}
