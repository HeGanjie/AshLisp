(def + (lambda (. x) (reduce add (car x) (cdr x))))

(def - (lambda (. x) (reduce sub (car x) (cdr x))))

(def * (lambda (. x) (reduce mul (car x) (cdr x))))

(def / (lambda (. x) (reduce div (car x) (cdr x))))

(def % (lambda (x y) (mod x y)))

(def < (lambda (x y) (lt x y)))

(def <= (lambda (x y) (le x y)))

(def > (lambda (x y) (gt x y)))

(def >= (lambda (x y) (ge x y)))

(def even? (lambda (x) (eq 0 (mod x 2))))

(def odd? (lambda (x) (eq 1 (mod x 2))))
