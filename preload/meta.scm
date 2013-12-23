(def defmacro (lambda (pattern template)
		(.new-macro (car pattern)
			    (cons pattern (cons template '())))))

(defmacro '(comment *) ''())

(defmacro '(defn fname args body)
  '(def fname (lambda args body)))

(defmacro '(lazy-cons head body)
  '(.stream head (lambda () body)))
