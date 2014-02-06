(defn = (h s . x)
      (when (eq h s)
	(if x
	  (apply = (cons s x))
	  't)))
(defn != (. x) (not (apply = x)))
(defn + (. x) (reduce add (car x) (cdr x)))
(defn - (. x) (reduce sub (car x) (cdr x)))
(defn * (. x) (reduce mul (car x) (cdr x)))
(defn / (. x) (reduce div (car x) (cdr x)))
(def % mod)

(defn && (. x) (reduce and (car x) (cdr x)))
(defn || (. x) (reduce or (car x) (cdr x)))

(def < lt)
(def <= le)
(def > gt)
(def >= ge)

(defn even? (x) (eq 0 (mod x 2)))
(defn odd? (x) (eq 1 (mod x 2)))
(defn inc (x) (add x 1))
(defn dec (x) (sub x 1))
(defn pos? (x) (lt 0 x))
(defn neg? (x) (lt x 0))
(defn zero? (x) (eq x 0))

