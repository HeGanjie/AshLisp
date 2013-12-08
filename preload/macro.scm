(def defmacro (lambda (pattern template)
		(.new-macro (car pattern) (list pattern template))))

(defmacro '(let (name val) body)
  '((lambda (name) body) val))

(defmacro '(if test true else)
  '(cond (test true)
	 ('t else)))
