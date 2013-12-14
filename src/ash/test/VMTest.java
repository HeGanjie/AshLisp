package ash.test;

import java.io.Serializable;

import junit.framework.TestCase;
import ash.compiler.Compiler;
import ash.lang.Node;
import ash.parser.Parser;
import ash.vm.VM;

public final class VMTest extends TestCase {
	private static VM vm = new VM();
	
	public void testEvalRaw() throws Exception {
		assertEquals(10, exec("10"));
	}
	
	public void testQuote() throws Exception {
		assertEquals(Node.NIL, exec("'()"));
		assertEquals("(quote a)", exec("''a").toString());
		
		assertEquals("(1 + () a 1)", exec("(list '1 '+ '() 'a 1)").toString());
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
		assertEquals("c", exec("(cond ((eq 1 2) 'a) ((eq 0 1) 'b) ('t 'c))"));
	}
	
	public void testLambda() throws Exception {
		assertEquals(Node.T, exec("((lambda (x) (eq 1 x)) 1)"));
		
		assertEquals(0, exec("(((lambda (f) f) add) 1 -1)"));
	}
	
	public void testMaths() throws Exception {
		assertEquals(10, exec("(+ 1 2 3 4)"));
		assertEquals(2, exec("(- 5 1 2)"));
		assertEquals(25, exec("(* 5 5)"));
		assertEquals(9, exec("(/ 45 5)"));
		assertEquals(2, exec("(% 42 5)"));
	}
	
	public void testFib() throws Exception {
		assertEquals(832040, exec("(fib 30)"));
	}
	
	public void testFibs() throws Exception {
		assertEquals(832040, exec("(fibs 30)"));
	}
	
	public void testFibTail() throws Exception {
		assertEquals(2178309, exec("(fib-tail 32)"));
	}

	public void testTailAndCombineArgs() throws Exception {
		exec("(def reverse0 (lambda (x y) (cond (x (tail (cdr x) (cons (car x) y))) ('t y))))");
		assertEquals("(1 0)", exec("(reverse0 (range 0 2) '())").toString());
		
		exec("(def reverse1 (lambda (x . y) (cond (x (tail (cdr x) (cons (car x) y))) ('t y))))");
		assertEquals("((1 (0 ())))", exec("(reverse1 (range 0 2) '())").toString());
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
	
	public void testContinuation() throws Exception {
		assertEquals(3628800, exec("(fac-c 10 identity)"));
	}
	
	public void testMacro() throws Exception {
		assertEquals("(let (a 1) a)", exec("'(let (a 1) a)").toString());
		assertEquals("((lambda (a) a) 1)", exec("(.expand-macro '(let (a 1) a))").toString());
		assertEquals(100, exec("(let (a 100) a)"));
	}
	
	protected static Serializable exec(String code) {
		return vm.runInMain(Compiler.astsToInsts(Parser.split(code)));
	}
}
