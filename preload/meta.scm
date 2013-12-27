(def defmacro (lambda (pattern template)
		(.new-macro (car pattern)
			    (cons pattern (cons template '())))))

(defmacro '(comment *) ''())

(defmacro '(defn fname args body)
  '(def fname (lambda args body)))

(defmacro '(lazy-seq body)
  '(.new ash.lang.LazyNode (lambda () body)))
