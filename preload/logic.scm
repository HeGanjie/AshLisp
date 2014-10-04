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
	  	((lvar? lv) (let (v (assoc lv s))
	  				 (if v (walk v s) lv)))
	  	('t lv)))

(defn unify (a b s)
	  (let (a (walk a s)
	  		b (walk b s))
	  	(cond
	  	  ((= a b) s)
	  	  ((var? a) (ext-s-nc a b s))
	  	  ((var? w) (ext-s-nc b a s))
	  	  ((&& (seq? a) (seq? b))
	  	   (let (uf (unify (car a) (car b) s))
	  	   	 (when uf (unify (cdr a) (cdr b) uf))))
	  	  ((eqv? a b ) s))))

