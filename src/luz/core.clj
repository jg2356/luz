(ns luz.core
  (:require [luz.fuzzy :refer :all])
  (:gen-class))

(def fuzzy-logic
  (create-fuzzy-logic))

(def world
  (create-environment :world))

(def light1
  (create-fuzzy-light world :light1 0 1000 50 fuzzy-logic))

(def sun (create-light world :sun 0))

(swap! (world :thermal-sensor) + 1)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
