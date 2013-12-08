(def fac (lambda (x)
	   (if (eq x 1) 1
	     (mul x (fac (sub x 1))))))

(def fib (lambda (x)
	   (if (lt x 2) x
	     (add (fib (sub x 1))
		  (fib (sub x 2))))))

(def fibs (lambda (x)
	    (if (< x 2) x
	      (+ (fibs (- x 1))
		 (fibs (- x 2))))))

(def fib-tail (lambda (n) (let (fib-iter (lambda (x a b)
					   (if
					     (eq 0 x) a
					     (tail (sub x 1) b (add a b)))))
			    (fib-iter n 0 1))))

(def fac-c (lambda (n f)
	     (if (zero? n) (f 1)
	       (tail (dec n)
		     (lambda (x) (f (* n x)))))))
