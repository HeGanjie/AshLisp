(def defmacro (lambda (pattern template)
		(.new-macro (car pattern)
			    (cons pattern (cons template '())))))

(defmacro '(comment *) ''())

(defmacro '(lazy-cons head body)
  '(.stream head (lambda () body)))

(defmacro '(defn fname args body)
  '(def fname (lambda args body)))

(defmacro '(let (*) body)
  '((lambda (%1) @body) %2))

(defmacro '(if test true else)
  '(cond (test true)
	 ('t else)))

(defmacro '(if-not test true else)
  '(cond ((not test) true)
	 ('t else)))

(defmacro '(when test true)
  '(cond (test true)))

(defmacro '(when-not test true)
  '(cond ((not test) true)))

(defmacro '(+ x *) '(add @x %))
(defmacro '(- x *) '(sub @x %))
(defmacro '(* x *) '(mul @x %))
(defmacro '(/ x *) '(div @x %))

(defmacro '(&& x *) '(and @x %))
(defmacro '(|| x *) '(or @x %))

(defmacro '(= x y) '(eq x y))
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

