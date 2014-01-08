(def firstPlainTextPattern (regex "(\S+)\s*"))

(defn split (code)
      (let (trim (.trim code))
	(if (zero? (count trim)) '()
	  (if (= \' (car trim))
	    (let (rest (get-rest trim (inc (count first)))
		  first (get-first (cdr trim)))
	      (cons (cons 'quote (split first))
		    (split rest)))
	    (let (rest (get-rest
