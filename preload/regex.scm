(def regex java.util.regex.Pattern/compile)

(def re-matcher .matcher)

(defn re-find (m)
      (when (.find m)
	(re-groups m)))

(defn re-groups (m)
      (let (gc (.groupCount m))
	(if (zero? gc)
	  (.group m 0)
	  (map (lambda (n) (.group m n)) (range 0 (inc gc))))))

(defn re-find-all (m)
      (lazy-seq
	(when (.find m)
	  (cons (re-groups m)
		(re-find-all m)))))

(defn re-seq (reg src)
      (re-find-all (re-matcher reg src)))

