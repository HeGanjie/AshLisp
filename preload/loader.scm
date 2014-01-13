(def compile ash.compiler.Compiler/compileSingle)

(def vmexec (lambda (insts)
	      (.runInMain (.new ash.vm.VM) insts)))

(def parse ash.parser.Parser/split)

(def lazy-load (lambda (astIter)
		 (.new ash.lang.LazyNode
		       (lambda ()
			 (cond ((.hasNext astIter) (cons (vmexec (compile (.next astIter)))
							 (lazy-load astIter))))))))

(def list (lambda (. x) x))

(def apply .applyTo)

(def eval (lambda (ast)
	    (vmexec (compile ast))))

(def doall (lambda (lazy) (apply list lazy)))

(def read-resource-file bruce.common.utils.FileUtil/readTextFileForDefaultEncoding)

(def load-src (lambda (src)
		(doall
		  (lazy-load
		    (.iterator
		      (parse src))))))

(def load (lambda (srcName)
	    (load-src
	      (read-resource-file srcName))))

(load "meta.scm")
(load "macro.scm")
(load "maths.scm")
(load "utils.scm")
(load "regex.scm")
(load "user.scm")
