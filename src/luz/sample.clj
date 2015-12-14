(ns luz.sample)

(use 'luz.core)

(def fuzzy-logic
  (create-fuzzy-logic))

(def world
  (create-environment :world))

(def sun (create-light world :sun 50))

(def light1
  (create-fuzzy-light world :light1 0 1000 10 fuzzy-logic))

(swap! (world :thermal-sensor) + 1)
