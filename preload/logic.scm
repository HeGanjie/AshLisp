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
	  	  ((nil? v) v)
	  	  ((seq? v) (cons (walk* (car v) s)
	  	  				  (walk* (cdr v) s)))
	  	  ('t v))))

(defn unify (a b s)
	  (let (a (walk a s)
	  		b (walk b s))
	  	(cond
	  	  ((eqv? a b) s)
	  	  ((lvar? a) (ext-s-nc a b s))
	  	  ((lvar? b) (ext-s-nc b a s))
	  	  ((&& (seq? a) (seq? b))
	  	   (let (uf (unify (car a) (car b) s))
	  	   	 (when uf (unify (cdr a) (cdr b) uf))))
	  	  ((= a b) s))))

