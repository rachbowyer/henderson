;; Copyright ©️ Rachel Bowyer 2015, 2021.
;; Please see the LICENSE file for more information

(ns henderson.core-test
  (:require
    [clojure.test :refer :all]
    [henderson.core :as hc]))


;;;; Frames

(deftest frame-coord-map-test
  (is (= (hc/make-vect 25 25)
      ((#'hc/frame-coord-map (#'hc/make-frame [5 5] [0 2] [2 0])) [10 10]))))

;;;; Painters

(def ^:private test-painter1
  (hc/segments-painter [(hc/make-segment [0 0] [1 1])]))

(def ^:private test-painter2
  (hc/segments-painter [(hc/make-segment [0 1] [1 0])]))

(def ^:private test-frame (#'hc/make-frame [4 4] [0 2] [2 0]))

(defn- mock-draw-lines-fn [do-effect]
  (let [lines (atom [])]
        (with-redefs [hc/draw-line (fn [g s e] (swap! lines conj [s e]))]
          (do-effect)
          @lines)))

(defmacro mock-draw-lines [arg1 arg2]
  `(let [~'lines (mock-draw-lines-fn  (fn [] ~arg1))]
     ~arg2))

(deftest segments-painter-test
  (mock-draw-lines
    (let [painter (hc/segments-painter [(hc/make-segment [4 4] [8 8])])]
      (painter nil test-frame))
      (is (= [[[12 12] [20 20]]] lines))))

(deftest beside-test
  (mock-draw-lines
    ((hc/beside test-painter1 test-painter2) nil test-frame)
    (is (= [[[4.0 4.0] [6.0 5.0]] [[6.0 5.0] [4.0 6.0]]] lines))))

(deftest below-test
  (mock-draw-lines
    ((hc/below test-painter1 test-painter2) nil test-frame)
    (is (= [[[5.0 4.0] [6.0 6.0]] [[5.0 4.0] [4.0 6.0]]] lines))))

(deftest flip-vert-test
  (mock-draw-lines
    ((hc/flip-vert test-painter1) nil test-frame)
    (is (= [[[6.0 4.0] [4.0 6.0]]] lines))))

(deftest flip-horiz-test
  (mock-draw-lines
    ((hc/flip-horiz test-painter1) nil test-frame)
    (is (= [[[4.0 6.0] [6.0 4.0]]] lines))))

(deftest rotate90-test
  (mock-draw-lines
    ((hc/rotate90 test-painter1) nil test-frame)
    (is (= [[[4.0 6.0] [6.0 4.0]]] lines))))

(deftest rotate180-test
  (mock-draw-lines
    ((hc/rotate180 test-painter1) nil test-frame)
    (is (= [[[6.0 6.0] [4.0 4.0]]] lines))))

(deftest right-split-test
  (mock-draw-lines
    ((hc/right-split test-painter1 1) nil test-frame)
    (is (= [[[4.0 4.0] [6.0 5.0]] [[5.0 5.0] [6.0 6.0]] [[4.0 5.0] [5.0 6.0]]]
           lines))))

(deftest up-split-test
  (mock-draw-lines
    ((hc/up-split test-painter1 1) nil test-frame)
    (is (= [[[5.0 4.0] [6.0 6.0]] [[4.0 4.0] [5.0 5.0]] [[4.0 5.0] [5.0 6.0]]]
           lines))))