(ns luz.util)

(defn between?
  [min-v v max-v]
  (and (<= v max-v)
       (< min-v v)))
