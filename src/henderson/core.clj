;; Copyright ©️ Rachel Bowyer 2015, 2021.
;; Please see the LICENSE file for more information

(ns henderson.core
  (:import (javax.swing JFrame JPanel)
           (java.awt Dimension)
           (java.awt.geom AffineTransform)))


(def ^:private border 5.0)


;
; Implementation of vectors (as a pair consisting of an x and y co-ord)
;

(defn make-vect [x y]
  [x y])

(defn- xcor-vect [vec]
  (get vec 0))

(defn- ycor-vect [vec]
  (get vec 1))

(defn- add-vect [a b]
  (make-vect
    (+  (xcor-vect a) (xcor-vect b))
    (+  (ycor-vect a) (ycor-vect b))))

(defn- sub-vect [a b]
  (make-vect
    (-  (xcor-vect a) (xcor-vect b))
    (-  (ycor-vect a) (ycor-vect b))))

(defn- scale-vect [scalar vect]
  (make-vect
    (* scalar (xcor-vect vect))
    (* scalar (ycor-vect vect))))


;
; Implementation of frames (as three vectors)
;

(defn- make-frame [origin edge1 edge2]
  [origin edge1 edge2])

(defn- origin-frame [frame]
  (get frame 0))

(defn- edge1-frame [frame]
  (get frame 1))

(defn- edge2-frame [frame]
  (get frame 2))

; Returns a function that takes a vector in the unit square and maps
; it -to co-ords in the provided frame
(defn frame-coord-map [frame]
  (fn [v]
    (add-vect
      (origin-frame frame)
      (add-vect
        (scale-vect (xcor-vect v) (edge1-frame frame))
        (scale-vect (ycor-vect v) (edge2-frame frame))))))


;
; Implementation of segments
;

(defn make-segment [start end]
  [start end])

(defn- start-segment [segment]
  (get segment 0))

(defn- end-segment [segment]
  (get segment 1))



;
; Painters
;

(declare draw-line)

(defn transform-painter [painter origin corner1 corner2]
  (fn frame [graphics frame]
    (let [m (frame-coord-map frame)]
      (let [new-origin (m origin)]
        (painter graphics (make-frame
                            new-origin
                            (sub-vect (m corner1) new-origin)
                            (sub-vect (m corner2) new-origin)))))))

(defn segments-painter [segment-list]
  (fn [graphics frame]
    (doall (map
             (fn [segment]
               (draw-line graphics
                          ((frame-coord-map frame) (start-segment segment))
                          ((frame-coord-map frame) (end-segment segment))))
             segment-list))))

(defn beside [painter1 painter2]
  (let [split-point (make-vect 0.5 0.0)
        paint-left (transform-painter
                     painter1
                     (make-vect 0.0 0.0)
                     split-point
                     (make-vect 0.0 1.0))
        paint-right (transform-painter
                      painter2
                      split-point
                      (make-vect 1.0 0.0)
                      (make-vect 0.5 1.0))]
    (fn [graphics frame]
      (paint-left graphics frame)
      (paint-right graphics frame))))


(defn below [painter1 painter2]
  (let [split-point (make-vect 0.0 0.5)
        paint-bottom (transform-painter
                       painter1
                       split-point
                       (make-vect 1.0 0.5)
                       (make-vect 0.0 1.0))


        paint-top (transform-painter
                    painter2
                    (make-vect 0.0 0.0)
                    (make-vect 1.0 0.0)
                    split-point)]
    (fn [graphics frame]
      (paint-bottom graphics frame)
      (paint-top graphics frame))))


(defn flip-vert [painter]
  (transform-painter
    painter
    (make-vect 0.0 1.0)
    (make-vect 1.0 1.0)
    (make-vect 0.0 0.0)))

(defn flip-horiz [painter]
  (transform-painter
    painter
    (make-vect 1.0 0.0)
    (make-vect 0.0 0.0)
    (make-vect 1.0 1.0)))

(defn rotate90 [painter] ; rotates 90 anti-clockwise
  (transform-painter
    painter
    (make-vect 1.0 0.0)
    (make-vect 1.0 1.0)
    (make-vect 0.0 0.0)))

(defn rotate180 [painter]
  (rotate90 (rotate90 painter)))

(defn right-split [painter n]
  (if (= n 0)
    painter
    (let [smaller (right-split painter (- n 1))]
      (beside painter (below smaller smaller)))))

(defn up-split [painter n]
  (if (= n 0)
    painter
    (let [smaller (up-split painter (- n 1))]
      (below painter (beside smaller smaller)))))

(defn corner-split [painter n]
  (if (= n 0)
    painter
    (let [  up          (up-split painter (- n 1))
          right         (right-split painter (- n 1))
          top-left      (beside up up)
          bottom-right  (below right right)
          corner        (corner-split painter (- n 1))]
      (beside
        (below painter top-left)
        (below bottom-right corner)))))

(defn square-limit [painter n]
  (let [ quarter (corner-split painter n)
        half      (beside (flip-horiz quarter) quarter)]
    (below (flip-vert half) half)))

(defn square-of-four [tl tr bl br]
  (fn [painter]
    (let [
          top     (beside (tl painter) (tr painter))
          bottom  (beside (bl painter) (br painter))]
      (below bottom top))))

(defn flipped-pairs [painter]
  (let [painter2 (beside painter (flip-vert painter))]
    (below painter2 painter2)))

(defn shrink-to-upper-right [painter]
  (transform-painter painter
                     (make-vect 0.5 0.5)
                     (make-vect 1.0 0.5)
                     (make-vect 0.5 1.0)))

(defn squash-inwards [painter]
  (transform-painter painter
                     (make-vect 0.0 0.0)
                     (make-vect 0.63 0.35)
                     (make-vect 0.35 0.65)))

(defn outline-painter [graphics frame]
  (let [s-p (segments-painter (list
                                (make-segment (make-vect 0 0) (make-vect 0 1))
                                (make-segment (make-vect 0 1) (make-vect 1 1))
                                (make-segment (make-vect 1 1) (make-vect 1 0))
                                (make-segment (make-vect 1 0) (make-vect 0 0))))]
    (s-p graphics frame)))


;
; Graphics support
;

(defn- draw-line [graphics point-a point-b]
  (. graphics drawLine
     (xcor-vect point-a) (ycor-vect point-a)
     (xcor-vect point-b) (ycor-vect point-b)))

(defn- render [g painter width height]
  (painter g
           (make-frame
             (make-vect border border)
             (make-vect (- width (* 2.0 border)) 0.0)
             (make-vect 0.0 (- height (* 2.0 border))))))

(defn- panel [painter width height]
  (doto (proxy [JPanel] []
          (paint [g] (render g painter width height)))
    (.setPreferredSize (new Dimension width height))))

(defn make-image-painter [file]
  (let
    [img (javax.imageio.ImageIO/read file)]

    (fn [graphics frame]
      (let [
            source-width (.getWidth img)
            source-height (.getHeight img)

            ;
            ; Writing the unit square into the frame can be achieved by using an affine
            ; transform defined
            ;
            ; ( edge1.x   edge2.x   orig.x)
            ; ( edge1.y   edge2.x   orig.y)
            ; (   0         0         1   )
            ;
            ; However, the source image is a different size to the unit square so we have
            ; to shrink the x co-ord by its width and the y co-ord by its height before
            ; applying the above affine transform. This can be done using the following
            ; affine transform
            ;
            ; ( 1/width    0          0 )
            ; (   0       1/height    0 )
            ; (   0       0           1 )
            ;
            ; We can compose the transforms
            ;  [map on to frame] . [shrink]
            ; to give the final affine transform
            ;
            ; ( edge1.x/width   edge2.x/height   orig.x)
            ; ( edge1.y/width   edge2.x/height   orig.y)
            ; (       0               0            1   )
            ;

            affine-transform (new AffineTransform
                                  (/ (xcor-vect (edge1-frame frame)) source-width)
                                  (/ (ycor-vect (edge1-frame frame)) source-width)
                                  (/ (xcor-vect (edge2-frame frame)) source-height)
                                  (/ (ycor-vect (edge2-frame frame)) source-height)
                                  (xcor-vect (origin-frame frame))
                                  (ycor-vect (origin-frame frame)))]
        (.drawImage graphics img affine-transform nil)))))

(defn show-picture [painter width height]
  (doto (new JFrame)
    (.add (panel painter width height)) .pack .show))