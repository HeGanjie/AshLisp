(defn iterate (f x)
  (lazy-cons x (iterate f (f x))))
  
(defn repeat (x)
  (lazy-cons x (repeat x)))