(ns luz.sample)

(use 'luz.core)

;;; create a default fuzzy logic with the default parameters:
;;; very-dark         100
;;; illuminated-min   0
;;; illuminated-max   2000
;;; very-light        1900
(def fuzzy-logic
  (create-fuzzy-logic))

;;; create an environment with id = :world
(def world
  (create-environment :world))

;;; create a light source to represent the sun with id = :sun
;;; and a light intensity of 50 (early morning?)
(def sun (create-light world :sun 50))

;;; create a fuzzy-light source with id = :light1
;;; with a minimum value of 0, maximum of 1000, and dimmer of 10
;;; and using the fuzzy-logic created earlier
(def light1
  (create-fuzzy-light world :light1 0 1000 10 fuzzy-logic))

;;; add one to the thermal sensor (it is 0 by default)
;;; and watch the output below
(swap! (world :thermal-sensor) + 1)

;;; :world thermal-sensor changed [old-value new-value] [0 1]
;;; :light1 turn up [current dim-value]:  [0 10]
;;; :world light-sensor changed [old-value new-value] [50 60]
;;; :light1 turn up [current dim-value]:  [10 10]
;;; :world light-sensor changed [old-value new-value] [60 70]
;;; :light1 turn up [current dim-value]:  [20 10]
;;; :world light-sensor changed [old-value new-value] [70 80]
;;; :light1 turn up [current dim-value]:  [30 10]
;;; :world light-sensor changed [old-value new-value] [80 90]
;;; :light1 turn up [current dim-value]:  [40 10]
;;; :world light-sensor changed [old-value new-value] [90 100]
;;; :light1 turn up [current dim-value]:  [50 10]
;;; :world light-sensor changed [old-value new-value] [100 110]
;;; :light1 maintain [current]:  [60]
