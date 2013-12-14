package ash.lang;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class MacroExpander {
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
		return createAstByTemplate(getNameAstMapping(pattern, ast), template);
	}

	private static Node createAstByTemplate(Map<String, Serializable> mapping, Node template) {
		if (template == Node.NIL) return Node.NIL;
		Serializable leftReplacement = null;
		if (template.left instanceof Node) {
			leftReplacement = createAstByTemplate(mapping, (Node) template.left);
		} else {
			if (template.left instanceof String)
				leftReplacement = mapping.get((String) template.left);
			if (leftReplacement == null)
				leftReplacement = template.left;
		}
		return new Node(leftReplacement, createAstByTemplate(mapping, template.next));
	}

	private static Map<String, Serializable> getNameAstMapping(Node pattern, Node ast) {
		Map<String, Serializable> mapping = new HashMap<>();
		findMapping(mapping, pattern.next, ast.next);
		return mapping;
	}

	private static void findMapping(Map<String, Serializable> mapping, Node pattern, Node ast) {
		if (pattern == Node.NIL) return;
		if (pattern.left instanceof Node)
			findMapping(mapping, (Node) pattern.left, (Node) ast.left);
		else
			mapping.put((String) pattern.left, ast.left);
		
		findMapping(mapping, pattern.next, ast.next);
	}
}
