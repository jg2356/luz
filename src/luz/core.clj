(ns luz.core
  (:require [clojure.set :refer :all])
  (:gen-class))

(defn between?
  [min-v v max-v]
  (and (<= v max-v)
       (< min-v v)))

(defn create-fuzzy-logic
  "Creates a fuzzy logic agent that operates"
  [& {:keys [very-dark
             illuminated-min
             illuminated-max
             very-light]
      :or {very-dark         400
           illuminated-min   200
           illuminated-max   2000
           very-light        1800}}]
  (fn [{:keys [id
               min-value
               max-value
               dim-value
               light-value]}
       light-sensor-value
       thermal-sensor-value]
    (let [status (cond (between? very-light light-sensor-value illuminated-max) :light
                       (between? illuminated-min light-sensor-value very-dark)  :dark
                       (< light-sensor-value very-dark)                         :very-dark
                       (> light-sensor-value very-light)                        :very-light
                       :else                                                    :illuminated)
          turn-max (fn [current]
                     (println "turn max [id current max-value]: " [id current max-value])
                     max-value)
          turn-min (fn [current]
                     (println "turn min [id current min-value]: " [id current min-value])
                     min-value)
          turn-up (fn [current]
                    (println "turn up [id current dim-value]: " [id current dim-value])
                    (if (< current max-value)
                      (+ current dim-value)
                      current))
          turn-down (fn [current]
                      (println "turn down [id current dim-value]: " [id current dim-value])
                      (if (> current min-value)
                        (- current dim-value)
                        current))
          maintain (fn [current]
                     (println "maintain [id current]: " [id current])
                     current)]
      (cond
        (> thermal-sensor-value 0)
        (case status
          :very-dark (swap! light-value turn-max)
          :dark      (swap! light-value turn-up)
          :illuminated (swap! light-value maintain)
          :light     (swap! light-value turn-down)
          :very-light (swap! light-value turn-min))
        :else (swap! light-value turn-min)))))

(defn create-environment
  "Creates an environment"
  [id]
  (let [light-sensor (atom 0)
        thermal-sensor (atom 0)
        light-sources (atom #{})]
    (add-watch
      light-sensor id
      (fn [id rf old-value new-value]
        (println "light-sensor changed [env old-value new-value]" [id old-value new-value])))
    (add-watch
      thermal-sensor id
      (fn [id rf old-value new-value]
        (println "thermal-sensor changed [env old-value new-value]" [id old-value new-value])))
    (add-watch
      light-sources id
      (fn [id rf old-value new-value]
        (let [added (difference new-value old-value)
              remvd (difference old-value new-value)]
          (doseq [{:keys [light-value]
                   :as light-source} remvd]
            (remove-watch light-value id)
            (swap! light-sensor - @light-value))
          (doseq [{:keys [light-value]} added]
            (add-watch
              light-value id
              (fn [id rf old-value new-value]
                (when-not (= old-value new-value)
                  (swap! light-sensor + (- new-value (or old-value nil))))))))))
    {:light-sensor light-sensor
     :thermal-sensor thermal-sensor
     :light-sources light-sources}))

(defn create-light
  [{:keys [light-sources]}
   id value]
  {:pre [(>= value 0)]}
  (let [light-value (atom 0)
        light-source {:id id
                      :light-value light-value}]
    (swap! light-sources conj light-source)
    (swap! light-value + value)
    light-source))

(defn create-fuzzy-light
  "Create a fuzzy light"
  [{:keys [light-sensor
           thermal-sensor
           light-sources]}
   id min-value max-value dim-value fuzzy-logic]
  {:pre [(>= min-value 0)
         (>= max-value 0)
         (> dim-value 0)
         (> max-value min-value)]}
  (let [light-value (atom 0)
        light-source {:id id
                      :min-value min-value
                      :max-value max-value
                      :dim-value dim-value
                      :light-value light-value}]
    (swap! light-sources conj light-source)
    (add-watch
      light-sensor id
      (fn [id rf old-value new-value]
        (when-not (= old-value new-value)
          (fuzzy-logic light-source new-value @thermal-sensor))))
    (add-watch
      thermal-sensor id
      (fn [id rf old-value new-value]
        (when-not (= old-value new-value)
          (fuzzy-logic light-source @light-sensor new-value))))
    (swap! light-value + min-value)
    light-source))

(defn remove-fuzzy-light
  "Remove a light"
  [{:keys [light-sensor
           thermal-sensor
           light-sources]}
   {:keys [id] :as light-source}]
  (remove-watch light-sensor id)
  (remove-watch thermal-sensor id)
  (swap! light-sources disj light-source))

(def fuzzy-logic
  (create-fuzzy-logic))

(def world
  (create-environment :world))

(def light1
  (create-fuzzy-light world :light1 0 1000 50 fuzzy-logic))

;(def sun (create-light world :sun 0))

(swap! (world :thermal-sensor) - 1)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
