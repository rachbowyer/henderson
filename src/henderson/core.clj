;; Copyright ©️ Rachel Bowyer 2015, 2021.
;; Please see the LICENSE file for more information

;;; Implementation of SCIP's Henderson Picture Language
;;; Note: origin is at the top left, positive x goes right
;;; and positive y goes down in accordance with Java2d

(ns henderson.core
  (:import
    (javax.swing JFrame JPanel)
    (java.awt Dimension Graphics2D)
    (java.awt.geom AffineTransform)
    (javax.imageio ImageIO)))

(def ^:private border 5.0)


;;;; Implementation of vectors (as a pair consisting of an x and y co-ord)

(defn make-vect
  "Creates a 2D mathematical vector"
  [x y]
  [x y])

(defn- xcor-vect [vect]
  (get vect 0))

(defn- ycor-vect [vect]
  (get vect 1))

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


;;;; Implementation of frames

(defn- make-frame [origin edge1 edge2]
  [origin edge1 edge2])

(defn- origin-frame [frame]
  (get frame 0))

(defn- edge1-frame [frame]
  (get frame 1))

(defn- edge2-frame [frame]
  (get frame 2))

(defn- frame-coord-map
  " Returns a function that takes a point in the unit square and maps
    it a point referenced by the frame (moving and scaling it).
    Co-ordinates of the new frame are returned wrt to (0,0)"
  [frame]
  (fn [v]
    (add-vect
      (origin-frame frame)
      (add-vect
        (scale-vect (xcor-vect v) (edge1-frame frame))
        (scale-vect (ycor-vect v) (edge2-frame frame))))))


;;;; Implementation of segments

(defn make-segment [start end]
  [start end])

(defn- start-segment [segment]
  (get segment 0))

(defn- end-segment [segment]
  (get segment 1))


;;;; Painters

(declare draw-line)
(declare load-image)
(declare draw-image)
(declare affine-transformation)

(defn- transform-painter
  "Given a transform on the unit square and a painter,
   produces a new painter.
   The frame passed into the new painter has the transform
   applied and is then passed to the old paper

   Transform is specified as follows:
    (0,0) -> origin
    (1,0) -> corner1
    (0,1) -> corner2"
  [painter origin corner1 corner2]
  (fn [graphics frame]
    (let [m (frame-coord-map frame)]
      (let [new-origin (m origin)]
        (painter graphics (make-frame
                            new-origin
                            (sub-vect (m corner1) new-origin)
                            (sub-vect (m corner2) new-origin)))))))

(defn segments-painter
  "Creates a painter that draws a list of line segments.
   Line segments are specified relative to the unit square, but
   are drawn within the requested frame."
  [segment-list]
  (fn [graphics frame]
      (let [fcm (frame-coord-map frame)]
          (doseq [segment segment-list]
            (draw-line graphics
                       (fcm (start-segment segment))
                       (fcm (end-segment segment)))))))

(defn beside
  "Creates two images side by side horizontally.
   The first image is on the left"
  [painter1 painter2]
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

(defn below
  "Stacks two images vertically.
   The first image is below"
  [painter1 painter2]
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

(defn flip-vert
  "Reflects the image in the Y axis"
  [painter]
  (transform-painter
    painter
    (make-vect 0.0 1.0)
    (make-vect 1.0 1.0)
    (make-vect 0.0 0.0)))

(defn flip-horiz
  "Reflects the image in the X axis"
  [painter]
  (transform-painter
    painter
    (make-vect 1.0 0.0)
    (make-vect 0.0 0.0)
    (make-vect 1.0 1.0)))

(defn rotate90
  "Rotates 90 degrees anti-clockwise"
  [painter]
  (transform-painter
    painter
    (make-vect 1.0 0.0)
    (make-vect 1.0 1.0)
    (make-vect 0.0 0.0)))

(defn rotate180 [painter]
  (rotate90 (rotate90 painter)))

(defn right-split
  "Splits and branches to the right recursively.
   n specifies how many times"
  [painter n]
  (if (zero? n)
    painter
    (let [smaller (right-split painter (dec n))]
      (beside painter (below smaller smaller)))))

(defn up-split
  "Splits and branches upwards recursively.
   n specifies how many times"
  [painter n]
  (if (zero? n)
    painter
    (let [smaller (up-split painter (dec n))]
      (below painter (beside smaller smaller)))))

(defn corner-split
  "Splits and branches right and upwards recursively
   n specifies how many times"
  [painter n]
  (if (zero? n)
    painter
    (let [  up          (up-split painter (dec n))
          right         (right-split painter (dec n))
          top-left      (beside up up)
          bottom-right  (below right right)
          corner        (corner-split painter (dec n))]
      (beside
        (below painter top-left)
        (below bottom-right corner)))))

(defn square-limit
  "Combines corner splits towards each corner
   n specifies how many times"
  [painter n]
  (let [ quarter (corner-split painter n)
        half      (beside (flip-horiz quarter) quarter)]
    (below (flip-vert half) half)))

(defn square-of-four
  "Combines four different transformations of painters
   and places one transformation of the painter in each
   corner.
   e.g. ((square-of-four rotate90
                         rotate180
                         flip-vert
                         flip-horiz)
         rogers)"
  [tl tr bl br]
  (fn [painter]
    (let [top     (beside (tl painter) (tr painter))
          bottom  (beside (bl painter) (br painter))]
      (below bottom top))))

(defn flipped-pairs
  "Creates two pairs of the painter. One pair is the correct way up.
   The other is upside down"
  [painter]
  (let [painter2 (beside painter (flip-vert painter))]
    (below painter2 painter2)))

(defn shrink-to-upper-right
  "Code matches SICP. But in this case due to the graphics co-ords, shrinks
   to the lower right"
  [painter]
  (transform-painter painter
                     (make-vect 0.5 0.5)
                     (make-vect 1.0 0.5)
                     (make-vect 0.5 1.0)))

(defn squash-inwards
  "Creates a diamond shaped image.
   Code matches SICP, but again the transformation looks slightly different
   due to the graphics co-ords"
  [painter]
  (transform-painter painter
                     (make-vect 0.0 0.0)
                     (make-vect 0.63 0.35)
                     (make-vect 0.35 0.65)))

(defn make-image-painter
  "Creates a painter from an image file such as a JPEG, TIFF or PNG"
  [file]
  (let
    [img (load-image file)]

    (fn [graphics frame]
      (let [
            source-width
            (.getWidth img)

            source-height
            (.getHeight img)

             ;;
             ;; Writing the unit square into the frame can be achieved by using
             ;; an affine transform defined
             ;;
             ;; ( edge1.x   edge2.x   orig.x)
             ;; ( edge1.y   edge2.x   orig.y)
             ;; (   0         0         1   )
             ;;
             ;; However, the source image is a different size to the unit square
             ;; so we have to shrink the x co-ord by its width and the y co-ord
             ;; by its height before applying the above affine transform. This
             ;; can be done using the following affine transform
             ;;
             ;; ( 1/width    0          0 )
             ;; (   0       1/height    0 )
             ;; (   0       0           1 )
             ;;
             ;; We can compose the transforms
             ;;  [map on to frame] . [shrink]
             ;; to give the final affine transform
             ;;
             ;; ( edge1.x/width   edge2.x/height   orig.x)
             ;; ( edge1.y/width   edge2.x/height   orig.y)
             ;; (       0               0            1   )


            affine-transform
            (affine-transformation
              (/ (xcor-vect (edge1-frame frame)) source-width)
              (/ (ycor-vect (edge1-frame frame)) source-width)
              (/ (xcor-vect (edge2-frame frame)) source-height)
              (/ (ycor-vect (edge2-frame frame)) source-height)
              (xcor-vect (origin-frame frame))
              (ycor-vect (origin-frame frame)))]

        (draw-image graphics img affine-transform)))))


;;;; Graphics support

(defn- load-image [file]
  (ImageIO/read file))

(defn- draw-image [^Graphics2D graphics img affine-transform]
  ;; Type hint required on graphics to avoid Illegal reflective access
  ;; warnings. i.e. To ensure interop works correctly.
  (.drawImage graphics img affine-transform nil))

(defn- affine-transformation [m00 m10 m01 m11 m02 m12]
  (new AffineTransform m00 m10 m01 m11 m02 m12))

;(defn- draw-line [^Graphics2D graphics point-a point-b]
;  (. graphics drawLine
;     (xcor-vect point-a) (ycor-vect point-a)
;     (xcor-vect point-b) (ycor-vect point-b)))

(defn- draw-line [^Graphics2D graphics point-a point-b]
  (.drawLine graphics
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
          (paintComponent [g] (render g painter width height)))
    (.setPreferredSize (new Dimension width height))))

(defn show-picture
  "Creates a Java Swing window rendering the painter.
   width and height are the dimensions of the window in pixels"
  [painter width height]
  (doto (new JFrame)
    (.add (panel painter width height)) .pack (.setVisible true)))