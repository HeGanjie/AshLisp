(def repl_br (->> (.$in System)
		  (.new java.io.InputStreamReader)
		  (.new java.io.BufferedReader)))

(def isStringNullOrWhiteSpace ash.util.JavaUtils/isStringNullOrWriteSpace)

(defn repl_loop (readIn)
      (when-not (isStringNullOrWhiteSpace readIn)
		(-> readIn load-src last ash.lang.BasicType/asString puts)
		(.print _out_ "> ")
		(tail (.readLine repl_br))))

(repl_loop "'(REPL is running, press ENTER to exit.)")

