package ash.lang;

import java.util.HashMap;
import java.util.Map;


public class MacroExpander {
	private static final Symbol MORE_ELEM = Symbol.create("*");
	private static final String PLACE_HERE_ELEM = "%";
	private static final String STACK_POINT_ELEM = "@";
	public static final Map<Symbol, Node> MARCOS_MAP = new HashMap<>();

	private MacroExpander() {}
	
	public static boolean hasMacro(Symbol symbol, Node ast) {
		Node pAndt = MARCOS_MAP.get(symbol);
		if (pAndt != null) {
			Node pattern = (Node) pAndt.head();
			return match(pattern, ast);
		}
		return false;
	}

	public static Node expand(Node ast) {
		Symbol macroName = (Symbol) ast.head();
		Node pAndt = MARCOS_MAP.get(macroName);
		Node pattern = (Node) pAndt.head();
		Node template = (Node) pAndt.rest().head();
		
		PersistentMap<Symbol, Object> mapping = findMapping(pattern.rest(), ast.rest());
		Node moreElem = (Node) mapping.get(MORE_ELEM);
		if (moreElem == null)
			return createAstByTemplate(mapping, template, null, null);
		return createAstRecur(mapping.dissoc(MORE_ELEM), template, moreElem, null, countReplacementRequire(template));
	}
	
	private static boolean match(PersistentList pattern, PersistentList ast) {
		if (pattern == BasicType.NIL) return ast == BasicType.NIL; // pattern ending
		
		if (pattern.head() instanceof Node) { // inner Node
			return ast.head() instanceof Node
					&& match((Node) pattern.head(), (Node) ast.head())
					&& match(pattern.rest(), ast.rest());
		} else if (pattern.head() instanceof Symbol) { // * x
			return MORE_ELEM.equals(pattern.head())
					|| ast != BasicType.NIL && match(pattern.rest(), ast.rest());
		} else {
			throw new IllegalArgumentException("Pattern Illegal!");
		}
	}

	private static Node createAstByTemplate(PersistentMap<Symbol, Object> mapping, PersistentList template, PersistentList moreElem, Node stack) {
		if (template == BasicType.NIL) return BasicType.NIL;
		
		Object leftReplacement = null;
		if (template.head() instanceof Node)
			leftReplacement = createAstByTemplate(mapping, (Node) template.head(), moreElem, stack);
		else {
			if (template.head() instanceof Symbol) {
				Symbol symbol = (Symbol) template.head();
				if (symbol.name.startsWith(PLACE_HERE_ELEM)) // %
					leftReplacement = getRequiringReplacement(symbol, moreElem);
				else if (symbol.name.startsWith(STACK_POINT_ELEM)) // @x
					leftReplacement = stack == null ? mapping.get(Symbol.create(symbol.name.substring(1))) : stack;
				else
					leftReplacement = mapping.get(symbol);
			}
			if (leftReplacement == null)
				leftReplacement = template.head();
		}
		return new Node(leftReplacement, createAstByTemplate(mapping, template.rest(), moreElem, stack));
	}
	
	private static Object getRequiringReplacement(Symbol symbol, PersistentList moreElem) {
		if (PLACE_HERE_ELEM.equals(symbol.name)) {
			return moreElem.head();
		} else {
			int drop = Integer.parseInt(symbol.name.substring(1)) - 1;
			Object replacement = ListUtils.drop(drop, moreElem).head();
			if (replacement == null)
				throw new IllegalArgumentException("Ast Illegal!");
			return replacement;
		}
	}

	private static Node createAstRecur(PersistentMap<Symbol, Object> mapping,
			Node template, PersistentList moreElem, Node stack, int replacementCount) {
		if (moreElem == BasicType.NIL) return stack;
		
		Node res = createAstByTemplate(mapping, template, moreElem, stack);
		if (replacementCount == 0) return res;
		return createAstRecur(mapping, template, ListUtils.drop(replacementCount, moreElem), res, replacementCount);
	}

	private static PersistentMap<Symbol, Object> findMapping(PersistentList pattern, PersistentList ast) {
		if (pattern == BasicType.NIL) return new PersistentMap<>();
		
		PersistentMap<Symbol, Object> leftMap;
		if (pattern.head() instanceof Node)
			leftMap = findMapping((Node) pattern.head(), (Node) ast.head());
		else {
			Symbol symbol = (Symbol) pattern.head();
			leftMap = new PersistentMap<>(symbol, MORE_ELEM.equals(symbol) ? ast : ast.head());
		}
		
		return leftMap.merge(findMapping(pattern.rest(), ast.rest()));
	}

	private static int countReplacementRequire(PersistentList template) {
		if (template == BasicType.NIL) return 0;
		int leftTreeCount = 0;
		if (template.head() instanceof Node)
			leftTreeCount = countReplacementRequire((Node) template.head());
		else {
			Object val = template.head();
			if (val instanceof Symbol && val.toString().startsWith(PLACE_HERE_ELEM))
				leftTreeCount = 1;
		}
		return leftTreeCount + countReplacementRequire(template.rest());
	}
}
