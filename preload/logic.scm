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

(defn ext-s-no-check (x v s)
	  (cons (list x v) s))

(defn walk (lv s)
	  (cond
	  	((lvar? lv) (let (v (assoc lv s))
	  				 (if v (walk v s) lv)))
	  	('t lv)))

