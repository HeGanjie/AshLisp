(defn reduce (func init seq)
      (if seq
	(tail func
	      (func init (car seq))
	      (cdr seq))
	init))

(defn mapr (func seq)
      (fold-right (lambda (item init) (cons (func item) init))
		  '()
		  seq))

(defn reverse (seq)
      (reduce (lambda (init item) (cons item init))
	      '()
	      seq))

(defn filter (pred seq)
      (lazy-seq
	(when seq
	  (let ((h . r) seq)
	    (if (pred h)
	      (cons h (filter pred r))
	      (filter pred r))))))

(defn map (func seq)
      (lazy-seq
	(when seq
	  (cons (func (car seq)) (map func (cdr seq))))))

(defn zip-step (cs)
      (lazy-seq
	(when (and cs (every identity cs))
	  (cons (map car cs) (zip-step (map cdr cs))))))

(defn zip (f . seqs)
      (map (lambda (args) (apply f args)) (zip-step seqs)))

(def nil? not)

(defn any (f seq)
      (when seq
	(if (f (car seq))
	  't
	  (tail f (cdr seq)))))

(defn complement (f)
      (lambda (. args) (not (apply f args))))

(defn partial (f . args)
      (lambda (. args0) (apply f (append args args0))))

(defn nth (n seq)
      (cond
	((nil? seq) '())
	((zero? n) (car seq))
	('t (tail (dec n) (cdr seq)))))

(defn ntree (nth-seq tree)
      (if nth-seq
	(tail (cdr nth-seq) (nth (car nth-seq) tree))
	tree))

(defn pair (keys vals)
      (cond
	((or (nil? keys) (nil? vals)) '())
	((eq (car keys) '.) (list (nth 1 keys) vals))
	('t (cons (list (car keys) (car vals)) (pair (cdr keys) (cdr vals))))))

(defn assoc (key pair-seq)
      (cond ((nil? pair-seq) '())
	    ((eq (ntree '(0 0) pair-seq) key)
	     (ntree '(0 1) pair-seq))
	    ('t (tail key (cdr pair-seq)))))

(defn count (seq) (reduce inc 0 seq))

(defn indexof (item seq skip)
      (cond ((nil? seq) -1)
	    ((eq (car seq) item) skip)
	    ('t (tail item (cdr seq) (inc skip)))))

(defn contains (item seq)
      (neq -1 (indexof item seq 0)))

(defn range (low high)
      (lazy-seq
	(when (< low high)
	  (cons low (range (inc low) high)))))

(def expand-macro ash.lang.MacroExpander/expand)

(defn identity (x) x)

(defn constantly (x)
      (lambda (. ignores) x))

(defn take (n seq)
      (lazy-seq
	(when seq 
	  (when-not (zero? n)
		    (cons (car seq) (take (dec n) (cdr seq)))))))

(defn drop (n seq)
      (lazy-seq
	(when seq
	  (if (zero? n) seq
	    (drop (dec n) (cdr seq))))))

(defn lazy-iterator (iter)
      (lazy-seq
	(when (.hasNext iter)
	  (cons (.next iter) (lazy-iterator iter)))))

(def sym ash.lang.Symbol/create)

(defn class (x)
      (sym (.getName (.getClass x))))

(defn partition (n coll)
      (lazy-seq
	(when coll
	  (cons (take n coll) (partition n (drop n coll))))))

(defn last (seq)
      (if (cdr seq)
	(tail (cdr seq))
	(car seq)))

(defn iterate (f x)
      (lazy-seq
	(cons x (iterate f (f x)))))

(defn repeat (x)
      (lazy-seq
	(cons x (repeat x))))

(defn stream-make (. args)
      (map identity args))

(defn str (. x)
      (bruce.common.utils.CommonUtils/displayArray
	(.toArray (.toList x))
	""))

(def _out_ (.$out System))

(defn puts (. x)
      (.println _out_ (apply str x)))

(defn num? (x) (instance? Number x))

(defn seq (x) (map identity x))

(defn lambda? (e)
      (instance? ash.vm.Closure e))

(defn trampoline (f . args)
      (if (lambda? f)
	(tail (apply f args))
	f))

(defn reductions (f init seq)
      (lazy-seq
	(if seq
	  (cons init
		(reductions f
			    (f init (car seq))
			    (cdr seq)))
	  (cons init '()))))

(defn lazy-cat (. cs)
      (lazy-seq
	(when cs
	  (let ((h . rc) cs)
	    (if h
	      (cons (car h)
		    (apply lazy-cat (cons (cdr h) rc)))
	      (apply lazy-cat rc))))))

(defn mapcat (f coll)
      (apply lazy-cat (map f coll)))

(defn keyword? (v)
      (instance? ash.lang.KeyWord v))

(defn take-while (f coll)
      (lazy-seq
	(when coll
	  (when (f (car coll))
	    (cons (car coll) (take-while f (cdr coll)))))))

(defn map-indexed (f s)
      (zip f (iterate inc 0) s))

