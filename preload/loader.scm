(def _vm_ (.new ash.vm.VM))

(def compile (lambda (ast)
	       (ash.compiler.Compiler/compileSingle ast)))

(def vmexec (lambda (insts)
	      (.runInMain _vm_ insts)))

(def parse (lambda (code)
	     (ash.parser.Parser/split code)))

(def lazy-load (lambda (astIter)
		 (.new ash.lang.LazyNode
		       (lambda ()
			 (cond ((.hasNext astIter) (cons (vmexec (compile (.next astIter)))
							 (lazy-load astIter))))))))

(def list (lambda (. x) x))

(def apply (lambda (func args)
	     (eval
	       (cons func
		     (ash.lang.ListUtils/quoteAll args)))))

(def eval (lambda (ast)
	    (vmexec (compile ast))))

(def doall (lambda (lazy) (apply list lazy)))

(def load (lambda (srcName)
	    (doall
	      (lazy-load
		(.iterator (parse
			     (bruce.common.utils.FileUtil/readTextFileForDefaultEncoding srcName)))))))

(load "meta.scm")
(load "macro.scm")
(load "maths.scm")
(load "utils.scm")
(load "user.scm")
