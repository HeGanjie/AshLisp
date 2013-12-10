(def defmacro (lambda (pattern template)
		(.new-macro (car pattern)
			    (cons pattern (cons template '())))))

(defmacro '(defn fname args body)
  '(def fname (lambda args body)))

(defmacro '(let (name val) body)
  '((lambda (name) body) val))

(defmacro '(if test true else)
  '(cond (test true)
	 ('t else)))

(defmacro '(when test true)
  '(cond (test true)))

(defmacro '(% x y) '(mod x y))
(defmacro '(< x y) '(lt x y))
(defmacro '(<= x y) '(le x y))
(defmacro '(> x y) '(gt x y))
(defmacro '(>= x y) '(ge x y))

(defmacro '(even? x) '(eq 0 (mod x 2)))
(defmacro '(odd? x) '(eq 1 (mod x 2)))
(defmacro '(inc x) '(add x 1))
(defmacro '(dec x) '(sub x 1))
(defmacro '(pos? x) '(lt 0 x))
(defmacro '(neg? x) '(lt x 0))
(defmacro '(zero? n) '(eq n 0))

