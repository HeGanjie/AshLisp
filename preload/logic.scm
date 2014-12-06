(def eq? eqv?)
(def equal? =)

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
	  	  (if ur (s# ur) (u# s)))))

