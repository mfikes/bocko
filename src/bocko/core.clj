(ns bocko.core
  (:import (javax.swing JFrame JPanel)
           (java.awt Color Dimension)))

(defprotocol IScreen
  "A screen to be drawn upon."
  (-clear [_] "Clears this screen to be black.")
  (-color [_ color] "Sets the color for plotting.")
  (-plot [_ x y] "Plots a point at (x, y).")
  (-hlin [_ x1 x2 y] "Plots a horizontal line from x1 to x2 at y.")
  (-vlin [_ y1 y2 x] "Plots a vertical line from y1 to y2 at x.")
  (-scrn [_ x y] "Gets the color at (x, y)."))

(def pixel-width 28)
(def pixel-height 16)

(defn- paint-point
  [g x y color]
  (.setColor g color)
  (.fillRect g (* x pixel-width) (* y pixel-height) pixel-width pixel-height))

(def color-map
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

(defn- color->awt-color
  [color]
  (color color-map))

(defn- plot-color
  [x y color g]
  (paint-point g x y (color->awt-color color)))

(defn- set-color
  [raster x y color]
  (swap! raster assoc-in [x y] color))

(defn- get-color
  [raster x y]
  (get-in @raster [x y]))

(defrecord JPanelScreen [panel width height color-atom raster]
  IScreen
  (-clear [_]
    (doseq [x (range width)
            y (range height)]
      (set-color raster x y :black)
      (.repaint panel)))
  (-color [_ color]
    (if (color color-map)
      (do
        (reset! color-atom color)
        nil)
      (str "Color must be from this list: " (sort (keys color-map)))))
  (-plot [_ x y]
    (set-color raster x y @color-atom)
    (.repaint panel))
  (-hlin [s x1 x2 y]
    (doseq [x (range x1 (inc x2))]
      (-plot s x y)))
  (-vlin [s y1 y2 x]
    (doseq [y (range y1 (inc y2))]
      (-plot s x y)))
  (-scrn [_ x y]
    (get-color raster x y)))

(defn- make-screen
  []
  (let [width 40
        height 40
        raster (atom (vec (repeat height (vec (repeat width :black)))))
        frame (JFrame. "Bocko")
        panel (proxy [JPanel] []
                (paintComponent [g]
                  (proxy-super paintComponent g)
                  (doseq [x (range width)
                          y (range height)]
                    (plot-color x y (get-in @raster [x y]) g)))
                (getPreferredSize []
                  (Dimension. (* width pixel-width)
                              (* height pixel-height))))
        screen (JPanelScreen.
                 panel
                 width
                 height
                 (atom :white)
                 raster)]
    (doto frame
      (.add panel)
      (.pack)
      (.setVisible true))
    screen))

(def ^:dynamic *screen* (make-screen))

(defn clear
  "Clears this screen to be black."
  []
  (-clear *screen*))

(defn color
  "Sets the color for plotting."
  [c]
  (-color *screen* c))

(defn plot
  "Plots a point at (x, y)."
  [x y]
  (-plot *screen* x y))

(defn hlin
  "Plots a horizontal line from x1 to x2 at y."
  [x1 x2 y]
  (-hlin *screen* x1 x2 y))

(defn vlin
  "Plots a vertical line from y1 to y2 at x."
  [y1 y2 x]
  (-vlin *screen* y1 y2 x))

(defn scrn
  "Gets the color at (x, y)."
  [x y]
  (-scrn *screen* x y))