(ns bocko.swing
  (:import (java.awt Dimension Color)
           (javax.swing JPanel JFrame)))

(defn- rgb->color
  [[r g b]]
  (Color. r g b))

(defn make-panel
  [color-map raster width height pixel-width pixel-height]
  (let [frame (JFrame. "Bocko")
        rgb->color (memoize rgb->color)
        paint-point (fn [x y c g]
                      (.setColor g (rgb->color (c color-map)))
                      (.fillRect g
                        (* x pixel-width) (* y pixel-height)
                        pixel-width pixel-height))
        panel (proxy [JPanel] []
                (paintComponent [g]
                  (proxy-super paintComponent g)
                  (let [r @raster]
                    (doseq [x (range width)
                            y (range height)]
                      (paint-point x y (get-in r [x y]) g))))
                (getPreferredSize []
                  (Dimension.
                    (* width pixel-width)
                    (* height pixel-height))))]
    (doto frame
      (.add panel)
      (.pack)
      (.setVisible true))
    (add-watch raster :monitor
      (fn [_ _ o n]
        (when (not= o n)
          (.repaint panel))))
    panel))
