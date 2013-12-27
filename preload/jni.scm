(defn seq (x) (.seq x))
(defn num? (x) (.instance? 'java.lang.Number x))
(defn str (. x) (apply .str x))
(defn puts (. x) (apply .puts x))