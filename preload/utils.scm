(defn && (. x) (reduce and (car x) (cdr x)))

(defn || (. x) (reduce or (car x) (cdr x)))

(defn reduce (func init seq)
      (if seq
	(tail func
	      (func init (car seq))
	      (cdr seq))
	init))

(defn fold-right (func init seq)
      (if seq
	(func (car seq)
	      (fold-right func init (cdr seq)))
	init))

(defn mapr (func seq)
      (fold-right (lambda (item init) (cons (func item) init))
		  '()
		  seq))

(defn reverse (seq)
      (reduce (lambda (init item) (cons item init))
	      '()
	      seq))

(defn map (func seq)
      (when seq
	(cons (func (car seq)) (map func (cdr seq)))))

(defn filter (pred seq)
      (fold-right (lambda (item init)
		    (if (pred item)
		      (cons item init)
		      init))
		  '()
		  seq))

(defn list (. x) x)

(defn nth (n seq)
      (cond
	((not seq) '())
	((eq n 0) (car seq))
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

(defn append (l r)
      (if l
	(cons (car l) (append (cdr l) r))
	r))

(defn count (seq) (reduce inc 0 seq))

(defn indexof (item seq skip)
      (cond ((not seq) -1)
	    ((eq (car seq) item) skip)
	    ('t (tail item (cdr seq) (inc skip)))))

(defn contains (item seq)
      (neq -1 (indexof item seq 0)))

(defn last (seq)
      (if (cdr seq)
	(car seq)
	(tail (cdr seq))))

(defn do (. things) (last things))

(defn range (low high)
      (when (< low high)
	(cons low (range (inc low) high))))

(defn apply (func args) (eval (cons func args)))

(defn eval (ast) (.vmexec (.compile ast)))

(defn identity (x) x)

