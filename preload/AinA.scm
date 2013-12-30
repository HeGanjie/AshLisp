(defn vector (. ls)
      (.new ash.lang.PersistentVector (. ls 'toList)))

(defn hash-set (. ls)
      (.new ash.lang.PersistentSet (. ls 'toList)))

(defn hash-map (. ls)
      (.new ash.lang.PersistentMap (. ls 'toList)))
