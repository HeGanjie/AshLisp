(defn iterate (f x)
      (lazy-seq
	(cons x (iterate f (f x)))))

(defn repeat (x)
      (lazy-seq
	(cons x (repeat x))))

(defn stream-make (. args)
      (map identity args))
