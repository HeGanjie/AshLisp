(defmacro cadr (s) `(car (cdr ~s)))

(defmacro caddr (s) `(car (cdr (cdr ~s))))

(defmacro cddr (s) `(cdr (cdr ~s)))

(defmacro if (test true else)
  `(cond (~test ~true)
	 ('t ~else)))

(defmacro if-not (test true else)
  `(cond ((not ~test) ~true)
	 ('t ~else)))

(defmacro do (. things)
  (if (cdr things)
    `((lambda (. rst) (last rst)) @things)
    `~(car things)))

(defmacro when (test . true)
  `(cond (~test (do @true))))

(defmacro when-not (test . true)
  `(cond ((not ~test) (do @true))))

(defmacro let (pvs . body)
  ((lambda (p v rest)
     (if rest
       `((lambda (~p) (let ~rest (do @body))) ~v)
       `((lambda (~p) (do @body)) ~v)))
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

(defmacro for (pcs body)
  (let (p (car pcs)
	coll (cadr pcs)
	rest (cddr pcs))
    (if rest
      (let (update-for (lambda (pu cu)
			 `(for (~pu ~cu @(cddr rest)) ~body)))
	(cond ((= :when (car rest)) (update-for p `(filter (lambda (~p) ~(cadr rest)) ~coll)))
	      ((= :while (car rest)) (update-for p `(take-while (lambda (~p) ~(cadr rest)) ~coll)))
	      ((= :let (car rest)) (let (letpv (zip-step (partition 2 (cadr rest)))
					 letp (car letpv)
					 letv (cadr letpv)
					 consp (if (seq? p) (concat p letp) (cons p letp))
					 consv (if (seq? p) (concat p letv) (cons p letv)))
				     (update-for consp `(map (lambda (~p) (list @consv)) ~coll))))
	      ((= :zip (car rest)) (let (zippv (zip-step (partition 2 (cadr rest)))
					 zipp (car zippv)
					 zipv (cadr zippv)
					 consp (if (seq? p) (concat p zipp) (cons p zipp))
					 consc (cons coll zipv))
				     (update-for consp `(zip-step (list @consc)))))
	      ('t `(mapcat (lambda (~p) (for ~rest ~body)) ~coll))))
      `(map (lambda (~p) ~body) ~coll))))

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

