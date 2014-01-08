(def _out_ (.$out System))

(def str (lambda (. x) (bruce.common.utils.CommonUtils/displayArray (.toArray (.toList x)) "")))

(def puts (lambda (. x) (.println _out_ (apply str x))))

(def last (lambda (seq)
	    (cond ((cdr seq) (tail (cdr seq)))
		  ('t (car seq)))))

(def do (lambda (. things) (last things)))

(def compile (lambda (ast)
	       (ash.compiler.Compiler/compileSingle ast)))

(def vmexec (lambda (insts) (.runInMain (.new ash.vm.VM) insts)))

(def parse (lambda (code)
	     (ash.parser.Parser/split code)))

(def lazy-load (lambda (astIter)
		 (.new ash.lang.LazyNode (lambda ()
					   (cond ((.hasNext astIter) (cons (vmexec (compile (.next astIter)))))
						  ('t (lazy-load astIter)))))))

(def list (lambda (. x) x))

(def apply (lambda (func args)
	     (eval
	       (cons func (ash.lang.ListUtils/quoteAll args)))))

(def eval (lambda (ast)
	    (vmexec (compile ast))))

(def doall (lambda (lazy) (apply list lazy)))

(def load (lambda (srcName)
	    (doall
	      (lazy-load
		(.iterator (parse
			     (bruce.common.utils.FileUtil/readTextFileForDefaultEncoding srcName)))))))

'(load "meta.scm")
'(load "AinA.scm")
'(load "macro.scm")
'(load "maths.scm")
'(load "lazy.scm")
'(load "utils.scm")
'(load "jni.scm")
'(load "user.scm")
