(def eq? eqv?)
(def equal? =)
(def procedure? fn?)

(defn pair? (s)
	  (and s (seq? s)))

(defmacro lvar (x)
  `(vector ~x))

(defmacro lvar? (x)
  `(vector? ~x))

(def lu (lvar 'u))
(def lv (lvar 'v))
(def lw (lvar 'w))

(def lx (lvar 'x))
(def ly (lvar 'y))
(def lz (lvar 'z))

(def empty-s '())

(defn ext-s-nc (x v s)
	  (cons (list x v) s))

(defn walk (lv s)
	  (cond
	  	((lvar? lv) (let (v (assv lv s))
	  				 (if v (walk v s) lv)))
	  	('t lv)))

(defn walk* (lv s)
	  (let (v (walk lv s))
	  	(cond
	  	  ((lvar? v) v)
	  	  ((pair? v) (cons (walk* (car v) s)
	  	  				  (walk* (cdr v) s)))
	  	  ('t v))))

(defn unify-nc (a b s)
	  (let (a (walk a s)
			b (walk b s))
		(cond
		  ((eq? a b) s)
		  ((lvar? a) (ext-s-nc a b s))
		  ((lvar? b) (ext-s-nc b a s))
		  ((and (pair? a) (pair? b))
		   (let (uf (unify-nc (car a) (car b) s))
			 (when uf (unify-nc (cdr a) (cdr b) uf))))
		  ((equal? a b) s))))

(defn reify-name (n)
	  (sym (str "_" n)))

(defn reify-s (lv s)
	  (let (v (walk lv s))
		(cond
		  ((lvar? v) (ext-s-nc v (reify-name (count s)) s))
		  ((pair? v) (reify-s (cdr v)
							 (reify-s (car v) s)))
		  ('t s))))

(defn reify (v)
	  (walk* v (reify-s v empty-s)))

(defn ext-s (x v s)
	  (cond
	  	((occurs x v s) '())
	  	('t (ext-s-nc x v s))))

(defn occurs (x v s)
	  (let (v (walk v s))
	  	(cond
	  	  ((lvar? v) (eq? v x))
	  	  ((pair? v) (or (occurs x (car v) s)
	  	  				(occurs x (cdr v) s))))))

(defn unify (v w s)
	  (let (v (walk v s)
	  		w (walk w s))
	  	(cond
	  	  ((eq? v w) s)
	  	  ((lvar? v) (if (lvar? w)
	  	  			   (ext-s-nc v w s)
	  	  			   (ext-s v w s)))
	  	  ((lvar? w) (ext-s w v s))
	  	  ((and (pair? v) (pair? w))
	  	   (let (uf (unify (car v) (car w) s))
	  	   	 (when uf (unify (cdr v) (cdr w) uf))))
	  	  ((equal? v w) s))))

(defn s# (s) (unit s))

(defn u# (s) (mzero))

(defn == (v w)
	  (lambda (s)
	  	(let (ur (unify v w s))
	  	  (if ur (unit ur) (mzero)))))

(defmacro mzero () `'())

(defmacro unit (a) `~a)

(defmacro choice (a f)
  `(cons ~a ~f))

(defmacro incr (e)
  `(lambda () ~e))

(defmacro run (N (q) . gs)
  `(let (n ~N x (lvar (quote ~q)))
  	 (when (or (not n) (< 0 n))
	   (map8 n
			 (lambda (s)
			   (reify (walk* x s)))
			 ((all @gs) empty-s)))))

(defmacro case8 (e (() e0) ((F) e1) ((A) e2) ((a f) e3))
  `(let (a8 ~e)
	 (cond
	   ((not a8) ~e0)
	   ((procedure? a8) (let (~F a8) ~e1))
	   ((and (pair? a8) (procedure? (cdr a8)))
	   	(let (~a (car a8) ~f (cdr a8)) ~e3))
	   ('t (let (~A a8) ~e2)))))

(defn map8 (n p a8)
	  (case8 a8 '()
	  		 ((a) (cons (p a) '()))
	  		 ((a f)
	  		  (cons (p a)
	  		  		(cond
	  		  		  ((not n) (map8 n p (f)))
	  		  		  ((< 1 n) (map8 (dec n) p (f))))))))

(defmacro conde ((g0 . g0s) (g1 . g1s) . gs)
  `(lambda (s)
  	 (inc (mplus* (bind* (~g0 s) @g0s)
  	 			  (bind* (~g1 s) @g1s) @gs))))

(defmacro all (g . gs)
  `(all-aux bind ~g @gs))

(defmacro all-aux (bnd . gs)
  (if-not gs s#
	  (let ((g0 . ggg) gs)
		(if-not ggg g0
				`(let (G ~g0)
				   (lambda (s)
					 (~bnd (G s)
						   (lambda (s) ((all-aux ~bnd @ggg) s)))))))))

(defn bind (a8 g)
	  (case8 a8
	  		 (() (mzero))
	  		 ((f) (inc (bind (f) g)))
	  		 ((a) (g a))
	  		 ((a f) (mplus (g a))
	  		 		(lambda () (bind (f) g)))))

(defmacro bind* (e . gs)
  (if-not gs `~e
  		  (let ((g0 . g1s) gs)
  		  	`(bind* (bind ~e ~g0) @g1s))))

(defn mplus (a8 f)
	  (case8 a8
			 (() (f))
			 ((F) (inc (mplus (f) F)))
			 ((a) (choice a f))
			 ((a F) (choice a (lambda () (mplus (f) F))))))

(defmacro mplus* (e0 . es)
  (if-not es `~e0
		  `(mplus ~e0 (lambda () (mplus* @es)))))

(defmacro exist ((x . xs) g0 . gs)
  `(lambda (s)
  	 (inc
  	   (let (~x (lvar '~x))
  	   	 (bind* (~g0 s) @gs)))))
