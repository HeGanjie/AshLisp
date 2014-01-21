(def _macrosMap_ (.$MARCOS_MAP ash.lang.MacroExpander))

(def new-macro (lambda (symbol fn) (.put _macrosMap_ symbol fn)))

(new-macro 'defmacro (lambda (name args body)
		       `(new-macro ~(list 'quote name) (lambda ~args ~body))))

(def fold-right (lambda (func init seq)
		  (cond (seq (func (car seq)
				   (fold-right func init (cdr seq))))
			('t init))))

(def concat (lambda (. cs)
	      (fold-right append '() cs)))

(def append (lambda (l r)
	      (cond (l (cond (r (cons (car l) (append (cdr l) r)))
			     ('t l)))
		    ('t r))))

(defmacro defn (fname args body)
  `(def ~fname (lambda ~args ~body)))

(defmacro lazy-seq (body)
  `(.new ash.lang.LazyNode (lambda () ~body)))

(defmacro comment (. ignore) `'())

(defn vector (. ls)
      (.new ash.lang.PersistentVector (.toList ls)))

(defn hash-set (. ls)
      (.new ash.lang.PersistentSet (.toList ls)))

(defn hash-map (. ls)
      (.new ash.lang.PersistentMap (.toList ls)))

(defn instance? (clazz val)
      (ash.vm.JavaMethod/instanceOf
	(.getClass val)
	(ash.vm.JavaMethod/loadClassBySymbol clazz)))

(defn seq? (obj) (instance? ash.lang.PersistentList obj))

(defn cadr (s) (car (cdr s)))

(defn caddr (s) (car (cdr (cdr s))))

(defn cddr (s) (cdr (cdr s)))

