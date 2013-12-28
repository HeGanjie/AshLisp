(defn num? (x) (instance? java.lang.Number x))

(defn str (. x) (bruce.common.utils.CommonUtils/displayArray (. x 'toArray) ""))

(defn puts (. x) (. *out* 'println (apply str x)))

(defn seq (x) (apply list x))

(defn instance? (clazz val)
  (ash.vm.JavaMethod/instanceOf
	(. val 'getClass)
	(java.lang.Class/forName (. clazz 'toString))))
	