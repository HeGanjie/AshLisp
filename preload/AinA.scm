(defn vector (. ls)
      (.new ash.lang.PersistentVector (.toList ls)))

(defn hash-set (. ls)
      (.new ash.lang.PersistentSet (.toList ls)))

(defn hash-map (. ls)
      (.new ash.lang.PersistentMap (.toList ls)))
