(defn num? (x) (instance? Number x))

(defn str (. x) (bruce.common.utils.CommonUtils/displayArray (.toArray (.toList x)) ""))

(defn puts (. x) (.println _out_ (apply str x)))

(defn seq (x) (map identity x))

(defn instance? (clazz val)
      (ash.vm.JavaMethod/instanceOf
	(.getClass val)
	(ash.vm.JavaMethod/loadClassBySymbol clazz)))

