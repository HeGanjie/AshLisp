(def not (lambda (x)
	   (cond ((eq x '()) 't)
		 ('t '()))))

(def and (lambda (x y)
	   (cond ((eq x '()) '())
		 ((eq y '()) '())
		 ('t 't))))

(def or (lambda (x y)
	  (cond ((eq x 't) 't)
		((eq y 't) 't)
		('t '()))))
