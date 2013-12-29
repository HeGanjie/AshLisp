(def *out* (.$ System 'out))
(def macrosMap (.$ ash.lang.MacroExpander 'MARCOS_MAP))

(def new-macro (lambda (symbol pAndt) (. macrosMap 'put symbol pAndt)))

(def defmacro (lambda (pattern template)
		(new-macro (car pattern)
			    (cons pattern (cons template '())))))

(defmacro '(comment *) ''())

(defmacro '(defn fname args body)
  '(def fname (lambda args body)))

(defmacro '(lazy-seq body)
  '(.new ash.lang.LazyNode (lambda () body)))
