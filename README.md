AshLisp
=======

A hobby Lisp compiler and runtime in Java/Android


## Usage

### Prepare
```Java
import ash.compiler.Compiler;
import ash.parser.Parser;
import ash.vm.VM;

public class AshLispUsage {
	private static VM vm = new VM();
	
	protected static Object eval(String code) {
		return vm.batchRunInMain(Compiler.batchCompile(Parser.parse(code)));
	}
	
	private static void trace(Object arg) {
		System.out.println(arg);
	}
	
	public static void main(String[] args) {
		...
	}
}
```

### Calculation
```Java
		trace(eval("(+ 1 2 3 4)")); // 10
		trace(eval("(* 2.5 3)")); // 7.5
```

### Logical operation 
```Java
		// support = < <= > >= or and && || eq not neq
		trace(eval("(< 1 2)")); // t
		trace(eval("(>= 2.5 3)")); // ()
		trace(eval("(&& 't '())")); // ()
		trace(eval("(|| 't '())")); // t
		trace(eval("(= (+ 1 1) 2)")); // t
```

### Define and Java native invoke
```Java
		trace(eval("(def x 10)")); // ()
		
		trace(eval("(.toString x)")); // "10"
		trace(eval("(Math/abs -10)")); // 10
		trace(eval("(.new Double \"-100\")")); // -100.0
		trace(eval("(instance? Number 1)")); // t
```

### Lisp operations
```Java
		trace(eval("(atom '())")); // t
		trace(eval("(car '(a b c))")); // a
		trace(eval("(cdr '(a b c))")); // (b c)
		trace(eval("(cons '+ '(1 2))")); // (+ 1 2)
		trace(eval("(cond ((= 1 2) 'a) ((= 0 1) 'b) ('t 'c))")); // c
```

### Function and Closure
```Java
		// define a function, equivalent to (def square (lambda (x) (* x x)))
		eval("(defn square (x) (* x x))");
		
		// call a function
		trace(eval("(square 10)")); // 100
		
		// make list from args
		trace(eval("((lambda (head . tail) tail) 10 20 30)")); // (20 30)
		
		// closure
		eval("(def func ((lambda (x) (lambda (y) (+ x y))) 10))");
		trace(eval("func")); // (lambda (y) (+ x y))
		trace(eval("(func -5)")); // 5
```

### Macro
```Java
		// macro.scm show you how to define macros using "defmacro"
		trace(eval("'(let (a 1) a)")); // (let (a 1) a)
		trace(eval("(expand-macro '(let (a 1) a))")); // ((lambda (a) a) 1)
		trace(eval("(let (a 1 b 2) (+ a b))")); // 3
```

### LazySeq
```Java
		// utils.scm show you how to define lazySeq
		trace(eval("(class (iterate inc 0))")); // ash.lang.LazyNode
		trace(eval("(take 10 (iterate inc 0))")); // (0 1 2 3 4 5 6 7 8 9)
```

### Destructuring
```Java
		trace(eval("((lambda ((a . b)) a) '(1 2 3))")); // 1
		trace(eval("((lambda ((a . b)) b) '(1 2 3))")); // (2 3)
```

### List Comprehension
```Java
		trace(eval("(for (a (range 0 6) :when (even? a)) a)")); // (0 2 4)
		trace(eval("(for (a (range 0 6) :let (b (* a 2))) [a b])"));
		// ([0 0] [1 2] [2 4] [3 6] [4 8] [5 10])
		trace(eval("(for (x [:a :b :c] y [1 2]) [x y])"));
		// ([:a 1] [:a 2] [:b 1] [:b 2] [:c 1] [:c 2])
		trace(eval("(for (a [:a :b :c :d] :zip (index (iterate inc 0))) [index a])"));
		// ([0 :a] [1 :b] [2 :c] [3 :d])
```

### Persistent Data Structure
```Java
		trace(eval("(class '(:a :b :c))")); // ash.lang.Node
		trace(eval("(class [1 2 3])")); // ash.lang.PersistentVector
		trace(eval("(class ${:a :b :c})")); // ash.lang.PersistentSet
		trace(eval("(class {:a 1 :b 2})")); // ash.lang.PersistentMap
		
		trace(eval("(.conj [0 1 2] 3)")); // [0 1 2 3]
		trace(eval("(.disj [:a :b :c] :a)")); // [:b :c]
		
		trace(eval("(get-methods [])"));
		/* ${.merge .conj .size .retainAll .get .assoc .getCollection
			.iterator .containsAll .toArray .disjAll .disjAt .toString
			.subList .indexOf .contains .isEmpty .disj .listIterator
			.hashCode .lastIndexOf .equals} */
```

### Misc
```Java
		// build string 
		trace(eval("(str \"No.\" 1)")); // No.1
		
		// some built-in functions (for more details, watch preload/*.scm)
		trace(eval("(filter even? (range 0 10))")); // (0 2 4 6 8)
		
		// loop: using tail recursion
		trace(eval("((lambda (ls) (when ls (puts (car ls)) (tail (cdr ls)))) (range 0 10))"));
		//println 0 1 2 ... 9 ()
		
		// eval & apply
		trace(eval("(eval '(+ 1 2))")); // 3
		trace(eval("(apply * '(3 4))")); // 12
```

### REPL
`ash.repl.REPLInVM` Run As Java Application

## Specification
* Doesn't support dotted pair
* Nil for false, others for true. Nil expressing into '()
* Preload file using '.scm' as suffix just for the convenience of editing and code coloring

