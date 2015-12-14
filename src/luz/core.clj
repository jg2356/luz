(ns luz.core
  (:require [luz.util :refer :all]
            [clojure.set :refer :all]))

(defn create-fuzzy-logic
  "Creates a fuzzy logic agent that operates"
  [& {:keys [very-dark
             illuminated-min
             illuminated-max
             very-light]
      :or {very-dark         100
           illuminated-min   0
           illuminated-max   2000
           very-light        1900}}]
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
                     (println id "turn max [current max-value]: " [current max-value])
                     max-value)
          turn-min (fn [current]
                     (println id "turn min [current min-value]: " [current min-value])
                     min-value)
          turn-up (fn [current]
                    (println id "turn up [current dim-value]: " [current dim-value])
                    (if (< current max-value)
                      (+ current dim-value)
                      current))
          turn-down (fn [current]
                      (println id "turn down [current dim-value]: " [current dim-value])
                      (if (> current min-value)
                        (- current dim-value)
                        current))
          maintain (fn [current]
                     (println id "maintain [current]: " [current])
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
        (when-not (= old-value new-value)
          (println id "light-sensor changed [old-value new-value]" [old-value new-value]))))
    (add-watch
      thermal-sensor id
      (fn [id rf old-value new-value]
        (when-not (= old-value new-value)
          (println id "thermal-sensor changed [old-value new-value]" [old-value new-value]))))
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
  "Create a light"
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
