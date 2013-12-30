(defn num? (x) (instance? Number x))

(defn str (. x) (bruce.common.utils.CommonUtils/displayArray (. (. x 'toList) 'toArray) ""))

(defn puts (. x) (. *out* 'println (apply str x)))

(defn seq (x) (apply list x))

(defn instance? (clazz val)
  (ash.vm.JavaMethod/instanceOf
	(. val 'getClass)
	(ash.vm.JavaMethod/loadClassBySymbol clazz)))
	