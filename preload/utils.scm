(def && (lambda (. x) (reduce and (car x) (cdr x))))

(def || (lambda (. x) (reduce or (car x) (cdr x))))

(def inc (lambda (n) (add n 1)))

(def dec (lambda (n) (sub n 1)))

(def reduce (lambda (func init seq)
	      (cond (seq (tail func
			       (func init (car seq))
			       (cdr seq)))
		    ('t init))))

(def fold-right (lambda (func init seq)
		  (cond (seq (func (car seq)
				   (fold-right func init (cdr seq))))
			('t init))))

(def mapr (lambda (func seq)
	    (fold-right (lambda (item init) (cons (func item) init)) '() seq)))

(def reverse (lambda (seq)
	       (reduce (lambda (init item) (cons item init)) '() seq)))

(def map (lambda (func seq)
	   (cond (seq (cons (func (car seq)) (map func (cdr seq)))))))

(def filter (lambda (pred seq)
	      (fold-right (lambda (item init) (cond ((pred item) (cons item init))
						    ('t init)))
			  '()
			  seq)))

(def list (lambda (. x) x))

(def nth (lambda (n seq)
	   (cond
	     ((not seq) '())
	     ((eq n 0) (car seq))
	     ('t (tail (sub n 1) (cdr seq))))))

(def ntree (lambda (nth-seq tree)
	     (cond (nth-seq (tail (cdr nth-seq) (nth (car nth-seq) tree)))
		   ('t tree))))

(def pair (lambda (keys vals)
	    (cond
	      ((or (not keys) (not vals)) '())
	      ((eq (car keys) '.) (list (nth 1 keys) vals))
	      ('t (cons (list (car keys) (car vals)) (pair (cdr keys) (cdr vals)))))))

(def assoc (lambda (key pair-seq)
	     (cond ((not pair-seq) '())
		   ((eq (ntree '(0 0) pair-seq) key)
		    (ntree '(0 1) pair-seq))
		   ('t (tail key (cdr pair-seq))))))

(def append (lambda (l r)
	      (cond (l (cons (car l) (append (cdr l) r)))
		    ('t r))))

(def count (lambda (seq) (reduce inc 0 seq)))

(def indexof (lambda (item seq skip)
	       (cond ((not seq) -1)
		     ((eq (car seq) item) skip)
		     ('t (tail item (cdr seq) (add skip 1))))))

(def contains (lambda (item seq) (neq -1 (indexof item seq 0))))

(def last (lambda (seq)
	    (nth (sub (count seq) 1) seq)))

(def do (lambda (. things) (last things)))

(def range (lambda (low high)
	     (cond ((lt low high) (cons low (range (add low 1) high))))))

(def apply (lambda (func args) (eval (cons func args))))

(def eval (lambda (ast) (.vmexec (.compile ast))))

(def identity (lambda (x) x))

(def zero? (lambda (x) (eq x 0)))
