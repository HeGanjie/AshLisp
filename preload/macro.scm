(defmacro cadr (s) `(car (cdr ~s)))

(defmacro caddr (s) `(car (cdr (cdr ~s))))

(defmacro cddr (s) `(cdr (cdr ~s)))

(defmacro if-not (test true else)
  `(cond ((not ~test) ~true)
	 ('t ~else)))

(defmacro when (test . true)
  `(cond (~test (do @true))))

(defmacro when-not (test . true)
  `(cond ((not ~test) (do @true))))

(defmacro let (pvs . body)
  ((lambda (p v rest)
     (if rest
       `((lambda (~p) (let ~rest @body)) ~v)
       `((lambda (~p) @body) ~v)))
   (car pvs) (cadr pvs) (cddr pvs)))

(defmacro -> (val . ops)
  (if ops
    (if (seq? (car ops))
      (let ((head . rest) (car ops))
	`(-> (~head ~val @rest) @(cdr ops)))
      `(-> (~(car ops) ~val) @(cdr ops)))
    val))

(defmacro ->> (val . ops)
  (if ops
    (if (seq? (car ops))
      (let ((head . rest) (car ops))
	`(->> (~head @rest ~val) @(cdr ops)))
      `(->> (~(car ops) ~val) @(cdr ops)))
    val))

(defmacro + (x y . s)
  (if s
    `(+ (add ~x ~y) @s)
    `(add ~x ~y)))

(defmacro - (x y . s)
  (if s
    `(- (sub ~x ~y) @s)
    `(sub ~x ~y)))

(defmacro * (x y . s)
  (if s
    `(* (mul ~x ~y) @s)
    `(mul ~x ~y)))

(defmacro / (x y . s)
  (if s
    `(/ (div ~x ~y) @s)
    `(div ~x ~y)))

(defmacro && (x y . s)
  (if s
    `(&& (and ~x ~y) @s)
    `(and ~x ~y)))

(defmacro || (x y . s)
  (if s
    `(|| (or ~x ~y) @s)
    `(or ~x ~y)))

(defmacro = (x y) `(eq ~x ~y))
(defmacro != (x y) `(neq ~x ~y))
(defmacro % (x y) `(mod ~x ~y))
(defmacro < (x y) `(lt ~x ~y))
(defmacro <= (x y) `(le ~x ~y))
(defmacro > (x y) `(gt ~x ~y))
(defmacro >= (x y) `(ge ~x ~y))

(defmacro even? (x) `(eq 0 (mod ~x 2)))
(defmacro odd? (x) `(eq 1 (mod ~x 2)))
(defmacro inc (x) `(add ~x 1))
(defmacro dec (x) `(sub ~x 1))
(defmacro pos? (x) `(lt 0 ~x))
(defmacro neg? (x) `(lt ~x 0))
(defmacro zero? (n) `(eq ~n 0))

