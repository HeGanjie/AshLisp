(def _out_ (.$out System))

(def macrosMap (.$MARCOS_MAP ash.lang.MacroExpander))

(def new-macro (lambda (symbol fn) (.put macrosMap symbol fn)))

(new-macro 'defmacro (lambda (name args body)
		       `(new-macro ~(list 'quote name) (lambda ~args ~body))))

(new-macro 'lazy-seq (lambda (body)
		       `(.new ash.lang.LazyNode (lambda () ~body))))

(new-macro 'comment (lambda (. ignore) '()))

(new-macro 'defn (lambda (fname args body)
		   `(def ~fname (lambda ~args ~body))))

(def list (lambda (. x) x))

(def fold-right (lambda (func init seq)
		  (cond (seq (func (car seq)
				   (fold-right func init (cdr seq))))
			('t init))))

(def concat (lambda (. cs)
	      (fold-right append '() cs)))

(def append (lambda (l r)
	      (cond (l (cons (car l) (append (cdr l) r)))
		    ('t r))))
