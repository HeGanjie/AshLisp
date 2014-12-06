(defn fac (x)
      (if (= x 1) 1
	(* x (fac (dec x)))))

(defn fibs (x)
      (if (< x 2) x
	(+ (fibs (dec x))
	   (fibs (- x 2)))))

(defn fib-tail (n) (let (fib-iter (lambda (x a b)
				    (if (zero? x) a
				      (tail (dec x) b (+ a b)))))
		     (fib-iter n 0 1)))

(defn fac-c (n f)
      (if (zero? n) (f 1)
	(tail (dec n)
	      (lambda (x) (f (* n x))))))

(defn sum (ls) (reduce add 0 ls))
