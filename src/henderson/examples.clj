;; Copyright ©️ Rachel Bowyer 2015, 2021.
;; Please see the LICENSE file for more information

(ns henderson.examples
  (:require [clojure.java.io :as io]
            [henderson.core :as hc]))

(def ^:private barton-filename "barton-head.jpg")

(def ^:private wave-data
  [[0.3 1.0  0.5 0.7]
   [0.7 1.0  0.5 0.7]
   [0.5 0.4  0.5 0.7]
   [0.5 0.0  0.3 0.2]
   [0.5 0.0  0.3 0.2]
   [0.5 0.0  0.7 0.2]
   [0.3 0.2  0.5 0.4]
   [0.7 0.2  0.5 0.4]
   [0.4 0.55 0.7 0.55]
   [0.4 0.55 0.3 0.4]])

(def wave
  "Picture of a character waving"
  (hc/segments-painter
       (map (fn [e] (hc/make-segment (hc/make-vect (get e 0) (get e 1))
                                     (hc/make-vect (get e 2) (get e 3))))
            wave-data)))

(def wave2
  "Picture of two characters side by side, one of whom is flipped upside down,
   waving"
  (hc/beside wave (hc/flip-vert wave)))

(def wave4
  "Picture of four characters waving"
  (hc/below wave2 wave2))

(def rogers
  "Picture of William Barton Rogers (first president MIT)"
  (hc/make-image-painter
   (io/resource barton-filename)))