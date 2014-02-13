(def _macrosMap_ (.$MARCOS_MAP ash.lang.MacroExpander))

(def new-macro (lambda* (symbol fn) (.put _macrosMap_ symbol fn)))

(new-macro 'defmacro (lambda* (name args body)
			      `(new-macro ~(list 'quote name) (lambda* ~args ~body))))

(def every (lambda* (f seq)
      (cond
	(seq (cond ((f (car seq)) (tail f (cdr seq)))))
	('t 't))))

(def fold-right (lambda* (func init seq)
		  (cond (seq (func (car seq)
				   (fold-right func init (cdr seq))))
			('t init))))

(def concat (lambda* (. cs)
	      (fold-right append '() cs)))

(def append (lambda* (l r)
	      (cond (l (cond (r (cons (car l) (append (cdr l) r)))
			     ('t l)))
		    ('t r))))

(defmacro lambda (params . body)
  (cond ((every atom params) `(lambda* ~params (do @body)))
	('t `(lambda* ~(map-indexed (lambda* (i p)
				      (if (atom p) p
					(sym (str \_  i))))
				    params)
		      (let ~(destructure params) @body)))))

(defmacro if (test true else)
  `(cond (~test ~true)
	 ('t ~else)))

(defmacro do (. things)
  (if (cdr things)
    `((lambda (. rst) (last rst)) @things)
    `~(car things)))

(defmacro defmacro (name args body)
  `(new-macro ~(list 'quote name) (lambda ~args ~body)))

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

(defn destructure-recur (i-stack dp)
      (let (uplevel-sym (sym (apply str (cons \_ i-stack)))
	    gen-drop (lambda (p index)
		       (if (= 1 index)
			 `(~p (cdr ~uplevel-sym))
			 `(~p (drop ~(dec index) ~uplevel-sym))))
	    gen-nth (lambda (p index)
		      (if (zero? index)
			`(~p (car ~uplevel-sym))
			`(~p (nth ~index ~uplevel-sym))))
	    this-layer (zip (lambda (index p check-dot)
			      (when (neq p '.)
				  (let (letp (if (atom p) p (sym (apply str `(\_ ~index @i-stack)))))
				    (if (= check-dot '.)
				      (gen-drop letp index)
				      (gen-nth letp index)))))
			    (iterate inc 0) dp (cons '() dp))
	    next-layer (map-indexed (lambda (index p)
				   (when-not (atom p)
					     (destructure-recur (cons index i-stack) p)))
				 dp))
	(append this-layer (apply concat next-layer))))

(defn destructure (ps)
      (apply concat
	     (apply concat
		    (map-indexed (lambda (index p)
				   (when-not (atom p)
					     (destructure-recur (list index) p)))
				 ps))))

