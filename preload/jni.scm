(defn num? (x) (instance? Number x))

(defn seq (x) (map identity x))

(defn instance? (clazz val)
      (ash.vm.JavaMethod/instanceOf
	(.getClass val)
	(ash.vm.JavaMethod/loadClassBySymbol clazz)))

