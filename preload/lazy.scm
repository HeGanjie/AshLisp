(defn iterate (f x)
      (lazy-cons x (iterate f (f x))))

(defn repeat (x)
      (lazy-cons x (repeat x)))

(defn stream-make (. args)
      (when args
	(lazy-cons (car args) (apply stream-make (cdr args)))))
