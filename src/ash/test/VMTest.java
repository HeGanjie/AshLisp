package ash.test;

import java.io.Serializable;

import junit.framework.TestCase;
import ash.compiler.Compiler;
import ash.parser.Node;
import ash.parser.Parser;
import ash.vm.VM;

public final class VMTest extends TestCase {
	private static Parser p = new Parser();
	private static VM vm = new VM();
	
	public void testEvalRaw() throws Exception {
		String rawExp = "10";
		assertEquals(10, exec(rawExp));
	}
	
	public void testQuote() throws Exception {
		String calc = "'()";
		assertEquals(Node.NIL, exec(calc));
		
		calc = "(list '1 '+ '() 'a 1)";
		assertEquals("(1 + () a 1)", exec(calc).toString());
	}
	
	public void testAtom() throws Exception {
		assertEquals(Node.T, exec("(atom 'a)"));
		assertEquals(Node.T, exec("(atom '())"));
		assertEquals(Node.NIL, exec("(atom '(a))"));
	}
	
	public void testEq() throws Exception {
		assertEquals(Node.T, exec("(eq 'a 'a)"));
		assertEquals(Node.T, exec("(eq 1 1)"));
		assertEquals(Node.NIL, exec("(eq 2 1)"));
		assertEquals(Node.T, exec("(eq '1 1)"));
		assertEquals(Node.T, exec("(eq '(a b (c)) '(a b (c)))"));
	}
	
	public void testCar() throws Exception {
		assertEquals("a", exec("(car '(a b c))"));
		assertEquals("+", exec("(car '(+ b c))"));
		assertEquals(new Node("a"), exec("(car '((a) b c))"));
	}
	
	public void testCdr() throws Exception {
		assertEquals(new Node("b", new Node("c")), exec("(cdr '(a b c))"));
		assertEquals(new Node(new Node("b"), new Node("c")), exec("(cdr '(a (b) c))"));
		assertEquals(Node.NIL, exec("(cdr '(a))"));
	}
	
	public void testCons() throws Exception {
		assertEquals("(+ a b c)", exec("(cons '+ '(a b c))").toString());
		assertEquals("(+ 1 2)", exec("(cons '+ (cons '1 '(2)))").toString());
		assertEquals(new Node(1), exec("(cons '1 '())"));
	}
	
	public void testCond() throws Exception {
		String exp = "(cond ((eq 1 2) 'a) ((eq 0 1) 'b) ('t 'c))";
		assertEquals("c", exec(exp));
	}
	
	public void testLambda() throws Exception {
		String exp = "((lambda (x) (eq 1 x)) 1)";
		assertEquals(Node.T, exec(exp));
		
		exp = "(((lambda (f) f) add) 1 -1)";
		assertEquals(0, exec(exp));
	}
	
	public void testMaths() throws Exception {
		assertEquals(10, exec("(+ 1 2 3 4)"));
		assertEquals(2, exec("(- 5 1 2)"));
		assertEquals(25, exec("(* 5 5)"));
		assertEquals(9, exec("(/ 45 5)"));
		assertEquals(2, exec("(% 42 5)"));
	}
	
	public void testFib() throws Exception {
		String calc = "(fib 30)";
		assertEquals(832040, exec(calc));
//		assertEquals(75025, exec(calc));
	}
	
	public void testFibs() throws Exception {
		String calc = "(fibs 30)";
		assertEquals(832040, exec(calc));
	}
	
	public void testFibTail() throws Exception {
		String calc = "(fib-tail 32)";
		assertEquals(2178309, exec(calc));
	}

	public void testTail() throws Exception {
		// TODO 
	}
	
	public void testClosure() throws Exception {
		String clojureExp2 = "(def func2 (lambda (x) (lambda (y z) (sub x (div y z))))) " +
				"(def g2 (func2 10)) (g2 30 6)";
		assertEquals(5, exec(clojureExp2));
		
		String clojureExp3 = "(def func3 (lambda (x) ((lambda (y z) (div x (sub y z))) 2 3))) " +
				"(func3 10)";
		assertEquals(-10, exec(clojureExp3));
		
		String clojureExp = "(def func1 (lambda (z) (lambda (y) (lambda (x) (div (sub x y) z))))) " +
				"(def f2 (func1 3)) (def f3 (f2 4)) (f3 10)";
		assertEquals(2, exec(clojureExp));
	}

	public void testPreload() throws Exception {
		assertEquals(5, exec("(count '(+ 1 2 3 4))"));
		assertEquals("(2 4)", exec("(filter even? '(1 2 3 4))").toString());
	}
	
	public void testString() throws Exception {
		assertEquals(" a 1", exec("(.str \" a \" 1)"));
		assertEquals(" ) 1", exec("(.str \" ) \" 1)"));
		assertEquals(" ( 1", exec("(.str \" ( \" 1)"));
		assertEquals(" a b c ", exec("\" a b c \""));
	}
	
	protected static Serializable exec(String code) {
		return vm.runInMain(Compiler.astsToInsts(p.split(code)));
	}
}
