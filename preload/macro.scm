(defmacro if (test true else)
  `(cond (~test ~true)
	 ('t ~else)))

(defmacro if-not (test true else)
  `(cond ((not ~test) ~true)
	 ('t ~else)))

(defmacro when (test . true)
  `(cond (~test (do @true))))

(defmacro when-not (test . true)
  `(cond ((not ~test) (do @true))))

(defmacro let ((p v . rest) body)
  (if rest
    `((lambda (~p) (let ~rest ~body)) ~v)
    `((lambda (~p) ~body) ~v)))

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

