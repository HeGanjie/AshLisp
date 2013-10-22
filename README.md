AshLisp
=======

A tiny Lisp compiler and runtime in Java


## Usage

### Prepare
```Java
import java.io.Serializable;
import ash.compiler.Compiler;
import ash.parser.Parser;
import ash.vm.VM;

public class AshLispUsage {
    private static Parser p = new Parser();
	private static VM vm = new VM(p);
	
	protected static Serializable eval(String code) {
		return vm.runInMain(Compiler.astsToInsts(p.split(code)));
	}
	
	private static void trace(Serializable arg) {
		System.out.println(arg);
	}
    
    public static void main(String[] args) {
        ...
    }
}
```

### Calculation
```Java
        trace(eval("(+ 1 2 3)")); // 6
    	trace(eval("(* 2.5 3)")); // 7.5
```

### Logical operation 
```Java
		// support < <= > >= or and && || eq not neq
		trace(eval("(< 1 2)")); // t
		trace(eval("(>= 2.5 3)")); // ()
		trace(eval("(&& 't '())")); // ()
		trace(eval("(|| 't '())")); // t
		trace(eval("(eq (+ 1 1) 2)")); // t
```

### Define and Java native invoke
```Java
		// define
		trace(eval("(def x 10)")); // ()
		
		// java native invoke, with dot prefix (define by yourself in ash.vm.JavaMethod)
		trace(eval("(.num? x)")); // t
```

### Lisp operations
```Java
		trace(eval("(atom '())")); // t
		trace(eval("(car '(a b c))")); // a
		trace(eval("(cdr '(a b c))")); // (b c)
		trace(eval("(cons '+ '(1 2))")); // (+ 1 2)
		trace(eval("(cond ((eq 1 2) 'a) ((eq 0 1) 'b) ('t 'c))")); // c
```

### Function and Closure
```Java
		// function
		trace(eval("((lambda (x) (* x 2))  10)")); // 20
		
		// closure
		eval("(def func ((lambda (x) (lambda (y) (+ x y))) 10))");
		trace(eval("(identity func)")); // ash.vm.Closure@17f74864
		trace(eval("(func -5)")); // 5
```

### Loop
```Java
    	// tail recursion for loop
		trace(eval("((lambda (ls) (cond (ls (do (.puts (car ls)) (tail (cdr ls)))))) (range 0 10))"));
        //println 0 1 2 ... 9 (last .puts return nil)
```

### Misc
```Java
		// eval
		trace(eval("(eval (cons + '(1 2)))")); // 3
		trace(eval("(apply * '(3 4))")); // 12
		
		// some built-in functions (for more details, watch preload/*.scm)
		trace(eval("(filter even? (range 0 10))")); // (0 2 4 6 8)
        
        // make list from args
    	trace(eval("((lambda (. ls) (identity ls)) 10 20 30)")); // (10 20 30)
```

## Specification
* List is the data structure only supported. (Doesn't support dotted pair)
* Doesn't support macro.
* Purely functional. (Defining the same symbol again will cause error)
* Nil for false, others for true. Nil expressing into '().
* Preload file using '.scm' as suffix just for the convenience of editing and color coding.
