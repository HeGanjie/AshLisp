package ash.lang;

import java.util.HashMap;
import java.util.Map;

import ash.compiler.Compiler;
import ash.vm.Closure;
import ash.vm.VM;


public final class MacroExpander {
	public static final Symbol SYNTAX_QUOTE = Symbol.create("syntax-quote");
	private static final Symbol LIST_SYMBOL = Symbol.create("list"),
								CONCAT_SYMBOL = Symbol.create("concat"),
								UNQUOTE_SYMBOL = Symbol.create("unquote"),
								UNQUOTE_SPLICING_SYMBOL = Symbol.create("unquote-splicing"),
								MORE_ELEM = Compiler.MULTI_ARGS_SIGNAL,
								QUOTE_SYMBOL = Symbol.create("quote");
	
	public static final Map<Symbol, Closure> MARCOS_MAP = new HashMap<>();

	private MacroExpander() {}
	
	public static boolean hasMacro(Node ast) {
		Closure closure = MARCOS_MAP.get(ast.head());
		if (closure == null) return false;
		return match(closure.getArgsList(), ast.rest());
	}
	
	private static boolean match(PersistentList pattern, PersistentList ast) {
		if (pattern.isEndingNode()) return ast.isEndingNode(); // pattern ending
		
		if (pattern.head() instanceof PersistentList) { // inner Node
			return ast.head() instanceof PersistentList
					&& match((PersistentList) pattern.head(), (PersistentList) ast.head())
					&& match(pattern.rest(), ast.rest());
		} else if (pattern.head() instanceof Symbol) {
			return MORE_ELEM.equals(pattern.head()) // . more
					|| !ast.isEndingNode() && match(pattern.rest(), ast.rest()); // x
		} else {
			throw new IllegalArgumentException("Pattern Illegal:\n" + pattern + "\n" + ast);
		}
	}
	
	private static Object applySyntaxQuote(Node visiting) {
		if (visiting.isEndingNode()) return BasicType.NIL;
		final Object head = ((Node) visiting).head();
		Object preListElem;
		PersistentList rest = (PersistentList) applySyntaxQuote((Node) visiting.rest());
		if (head instanceof Node) {
			Object headOfElem = ((Node) head).head();
			if (UNQUOTE_SYMBOL.equals(headOfElem)) { // ~(cdr '(1 2 3)) -> (unquote (cdr '(1 2 3))) -> (list (cdr '(1 2 3)))
				preListElem = ((Node) head).second();
			} else if (UNQUOTE_SPLICING_SYMBOL.equals(headOfElem)) { // @(cdr '(1 2 3)) -> (unquote-splicing (cdr '(1 2 3))) -> (cdr '(1 2 3))
				return new Node(((Node) head).second(), rest);
			} else {
				preListElem = new Node(CONCAT_SYMBOL, (PersistentList) applySyntaxQuote((Node) head));
			}
		} else if (head instanceof Symbol) {
			String name = ((Symbol) head).name;
			if (name.charAt(0) == '@') { // @a -> (concat a ...)
				return new Node(Symbol.create(name.substring(1)), rest);
			} else if (name.charAt(0) == '~') { // ~a -> (concat (list a) ...)
				preListElem = Symbol.create(name.substring(1));
			} else if (VM.tempVar.containsKey(head) && !MARCOS_MAP.containsKey(head)) { // for hygienic macro
				preListElem = VM.tempVar.get(head);
			} else
				preListElem = quoted(head); // val -> (concat (list 'val) ...)
		} else {
			preListElem = head; // 1 2.3 \a "asdf"
		}
		return new Node(new Node(LIST_SYMBOL, new Node(preListElem)), rest); // ((list elem) ...)
	}

	private static Node quoted(final Object val) {
		return new Node(QUOTE_SYMBOL, new Node(val));
	}
	
	public static Object visitSyntaxQuote(Object quoted) {
		if (quoted instanceof Node) {
			Node quotedNode = (Node) quoted;
			if (UNQUOTE_SYMBOL.equals(quotedNode.head())) // `~(...)
				return quotedNode.second();
			else
				return new Node(CONCAT_SYMBOL, (PersistentList) applySyntaxQuote((Node) quoted));
		}
		return quoted;
	}

	public static Object expand(Node ast) {
		Closure lambda = MacroExpander.MARCOS_MAP.get(ast.head());
		return lambda.applyTo(ast.rest());
	}
}
