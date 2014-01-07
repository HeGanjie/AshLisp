package ash.lang;

import java.util.HashMap;
import java.util.Map;

import ash.compiler.Compiler;
import ash.vm.Closure;
import ash.vm.VM;


public class MacroExpander {
	private static final Symbol LIST_SYMBOL = Symbol.create("list"),
								QUOTE_SYMBOL = Symbol.create("quote"),
								CONCAT_SYMBOL = Symbol.create("concat"),
								UNQUOTE_SYMBOL = Symbol.create("unquote"),
								UNQUOTE_SPLICING_SYMBOL = Symbol.create("unquote-splicing");
	public static final Map<Symbol, Closure> MARCOS_MAP = new HashMap<>();

	private MacroExpander() {}
	
	public static boolean hasMacro(Symbol macroName) {
		return MARCOS_MAP.containsKey(macroName);
	}
	
	private static Object applySyntaxQuote(Node visiting) {
		if (visiting == BasicType.NIL) return BasicType.NIL;
		final Object head = ((Node) visiting).head();
		Object preListElem;
		PersistentList rest = (PersistentList) applySyntaxQuote((Node) visiting.rest());
		if (head instanceof Node) {
			Object headOfElem = ((Node) head).head();
			if (UNQUOTE_SYMBOL.equals(headOfElem)) { // ~(cdr '(1 2 3)) -> (unquote (cdr '(1 2 3))) -> (list (cdr '(1 2 3)))
				preListElem = ((Node) head).rest().head();
			} else if (UNQUOTE_SPLICING_SYMBOL.equals(headOfElem)) { // @(cdr '(1 2 3)) -> (unquote-splicing (cdr '(1 2 3))) -> (cdr '(1 2 3))
				return new Node(((Node) head).rest().head(), rest);
			} else {
				preListElem = new Node(CONCAT_SYMBOL, (PersistentList) applySyntaxQuote((Node) head));
			}
		} else if (head instanceof Symbol) {
			String name = ((Symbol) head).name;
			if (name.charAt(0) == '@') { // @a -> (concat a ...)
				return new Node(Symbol.create(name.substring(1)), rest);
			} else if (name.charAt(0) == '~') { // ~a -> (concat (list a) ...)
				preListElem = Symbol.create(name.substring(1));
			} else
				preListElem = new Node(QUOTE_SYMBOL, new Node(head)); // val -> (concat (list 'val) ...)
		} else {
			preListElem = head; // 1 2.3 \a "asdf"
		}
		return new Node(new Node(LIST_SYMBOL, new Node(preListElem)), rest);
	}

	public static Object visitSyntaxQuote(Object quoted) {
		return quoted instanceof Node ? new Node(CONCAT_SYMBOL, (PersistentList) applySyntaxQuote((Node) quoted)) : quoted;
	}

	public static Object expandMacro(Symbol macroName, Node node) {
		Closure lambda = MacroExpander.MARCOS_MAP.get(macroName);
		// (fn args)
		Node exp = new Node(lambda, ListUtils.quoteAll(node.rest()));
		return new VM().runInMain(Compiler.compileSingle(exp));
	}
}
