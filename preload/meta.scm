(def *out* (.$out System))
(def macrosMap (.$MARCOS_MAP ash.lang.MacroExpander))

(def new-macro (lambda (symbol pAndt) (.put macrosMap symbol pAndt)))

(def defmacro* (lambda (pattern template)
		(new-macro (car pattern)
			    (cons pattern (cons template '())))))
			    
(defmacro* '(comment *) ''())

(defmacro* '(defn fname args body)
  '(def fname (lambda args body)))

(defmacro* '(lazy-seq body)
  '(.new ash.lang.LazyNode (lambda () body)))

(defmacro* '(defmacro p t) '(defmacro* (quote p) (quote t)))
