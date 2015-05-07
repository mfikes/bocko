(ns bocko.core
  (:import (javax.swing JFrame JPanel)
           (java.awt Color Dimension)))

(def ^:private width 40)
(def ^:private height 40)
(def ^:private pixel-width 28)
(def ^:private pixel-height 16)
(def ^:private clear-color :black)
(def ^:private default-color :white)
(def ^:private clear-screen (vec (repeat height (vec (repeat width clear-color)))))

(defonce ^:private raster (atom clear-screen))

(def ^:private color-map
  {:black       (Color. 0 0 0)
   :red         (Color. 157 9 102)
   :dark-blue   (Color. 42 42 229)
   :purple      (Color. 199 52 255)
   :dark-green  (Color. 0 118 26)
   :dark-gray   (Color. 128 128 128)
   :medium-blue (Color. 13 161 255)
   :light-blue  (Color. 170 170 255)
   :brown       (Color. 85 85 0)
   :orange      (Color. 242 94 0)
   :light-gray  (Color. 192 192 192)
   :pink        (Color. 255 137 229)
   :light-green (Color. 56 203 0)
   :yellow      (Color. 213 213 26)
   :aqua        (Color. 98 246 153)
   :white       (Color. 255 255 254)})

(defn clear
  "Clears this screen."
  []
  (reset! raster clear-screen)
  nil)

(def ^:dynamic *color* "The color used for plotting." default-color)

(defn- get-current-color
  []
  (let [c *color*]
    (if (contains? color-map c)
      c
      default-color)))

(defn- set-current-color
  [c]
  (if (thread-bound? #'*color*)
    (set! *color* c)
    (alter-var-root #'*color* (constantly c))))

(defn color
  "Sets the color for plotting.

  The color must be one of the following:

  :black        :red        :dark-blue    :purple
  :dark-green   :dark-gray  :medium-blue  :light-blue
  :brown        :orange     :light-gray   :pink
  :light-green  :yellow     :aqua         :white"
  [c]
  {:pre [(keyword? c)
         (c #{:black :red :dark-blue :purple
              :dark-green :dark-gray :medium-blue :light-blue
              :brown :orange :light-gray :pink
              :light-green :yellow :aqua :white})]}
  (set-current-color c)
  nil)

(defn plot
  "Plots a point at a given x and y.

  Both x and y must be between 0 and 39."
  [x y]
  {:pre [(integer? x) (integer? y) (<= 0 x 39) (<= 0 y 39)]}
  (swap! raster assoc-in [x y] (get-current-color))
  nil)

(defn- lin
  [x1 x2 y f]
  (if (< x2 x1)
    (lin x2 x1 y f)
    (let [c (get-current-color)]
      (swap! raster
        #(reduce (fn [r x]
                   (assoc-in r (f [x y]) c))
          %
          (range x1 (inc x2))))
      nil)))

(defn hlin
  "Plots a horizontal line from x1 to x2 at a given y.

  The x and y numbers must be between 0 and 39."
  [x1 x2 y]
  {:pre [(integer? x1) (integer? x2) (integer? y) (<= 0 x1 39) (<= 0 x2 39) (<= 0 y 39)]}
  (lin x1 x2 y identity))

(defn vlin
  "Plots a vertical line from y1 to y2 at a given x.

  The x and y numbers must be between 0 and 39."
  [y1 y2 x]
  {:pre [(integer? y1) (integer? y2) (integer? x) (<= 0 y1 39) (<= 0 y2 39) (<= 0 x 39)]}
  (lin y1 y2 x reverse))

(defn scrn
  "Gets the color at a given x and y.

  Both x and y must be between 0 and 39."
  [x y]
  {:pre [(integer? x) (integer? y) (<= 0 x 39) (<= 0 y 39)]}
  (get-in @raster [x y]))

(defn- make-panel
  [raster]
  (let [frame (JFrame. "Bocko")
        paint-point
        (fn [x y c g]
          (.setColor g (c color-map))
          (.fillRect g (* x pixel-width) (* y pixel-height) pixel-width pixel-height))
        panel (proxy [JPanel] []
                (paintComponent [g]
                  (proxy-super paintComponent g)
                  (let [r @raster]
                    (doseq [x (range width)
                            y (range height)]
                      (paint-point x y (get-in r [x y]) g))))
                (getPreferredSize []
                  (Dimension. (* width pixel-width)
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

(defonce ^:private panel (make-panel raster))