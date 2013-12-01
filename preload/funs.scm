(def fac (lambda (x)
	   (cond ((eq x 1) 1)
		 ('t (mul x (fac (sub x 1)))))))

(def fib (lambda (x)
	   (cond ((lt x 2) x)
		 ('t (add (fib (sub x 1))
			  (fib (sub x 2)))))))

(def fibs (lambda (x)
	    (cond ((< x 2) x)
		  ('t (+ (fibs (- x 1))
			 (fibs (- x 2)))))))

(def fib-tail (lambda (n) ((lambda (fib-iter) (fib-iter n 0 1))
			   (lambda (x a b)
			     (cond
			       ((eq 0 x) a)
			       ('t (tail (sub x 1) b (add a b))))))))

(def fac-c (lambda (n f)
	     (cond ((zero? n) (f 1))
		   ('t (tail (dec n)
			     (lambda (x) (f (* n x))))))))

