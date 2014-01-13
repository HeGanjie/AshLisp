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
      (fold-right (lambda (item init)
		    (if (pred item)
		      (cons item init)
		      init))
		  '()
		  seq))

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
      (fold-right (lambda (item init) (or (f item) init))
		  '() seq))

(defn every (f seq)
      (fold-right (lambda (item init) (and (f item) init))
		  't seq))

(defn complement (f)
      (lambda (. args) (not (apply f args))))

(defn partial (f . args)
      (lambda (. args0) (apply f (append args args0))))

(defn nth (n seq)
      (cond
	((not seq) '())
	((zero? n) (car seq))
	('t (tail (dec n) (cdr seq)))))

(defn ntree (nth-seq tree)
      (if nth-seq
	(tail (cdr nth-seq) (nth (car nth-seq) tree))
	tree))

(defn pair (keys vals)
      (cond
	((or (not keys) (not vals)) '())
	((eq (car keys) '.) (list (nth 1 keys) vals))
	('t (cons (list (car keys) (car vals)) (pair (cdr keys) (cdr vals))))))

(defn assoc (key pair-seq)
      (cond ((not pair-seq) '())
	    ((eq (ntree '(0 0) pair-seq) key)
	     (ntree '(0 1) pair-seq))
	    ('t (tail key (cdr pair-seq)))))

(defn count (seq) (reduce inc 0 seq))

(defn indexof (item seq skip)
      (cond ((not seq) -1)
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

(defn class (x)
      (ash.lang.Symbol/create (.getName (.getClass x))))

(defn partition (n coll)
      (lazy-seq
	(when coll
	  (cons (take n coll) (partition n (drop n coll))))))

(def last (lambda (seq)
	    (cond ((cdr seq) (tail (cdr seq)))
		  ('t (car seq)))))

(defn iterate (f x)
      (lazy-seq
	(cons x (iterate f (f x)))))

(defn repeat (x)
      (lazy-seq
	(cons x (repeat x))))

(defn stream-make (. args)
      (map identity args))

(def str (lambda (. x)
	   (bruce.common.utils.CommonUtils/displayArray (.toArray (.toList x)) "")))

(def puts (lambda (. x)
	    (.println _out_ (apply str x))))

(defn num? (x) (instance? Number x))

(defn seq (x) (map identity x))

