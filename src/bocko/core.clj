(ns bocko.core)

(def ^:private ^:const width 40)
(def ^:private ^:const height 40)
(def ^:private ^:const pixel-width 28)
(def ^:private ^:const pixel-height 16)
(def ^:private clear-color :black)
(def ^:private default-color :white)
(def ^:private clear-screen (vec (repeat height (vec (repeat width clear-color)))))

(defonce ^:private raster (atom clear-screen))

(def ^:private color-map
  {:black       [0 0 0]
   :red         [157 9 102]
   :dark-blue   [42 42 229]
   :purple      [199 52 255]
   :dark-green  [0 118 26]
   :dark-gray   [128 128 128]
   :medium-blue [13 161 255]
   :light-blue  [170 170 255]
   :brown       [85 85 0]
   :orange      [242 94 0]
   :light-gray  [192 192 192]
   :pink        [255 137 229]
   :light-green [56 203 0]
   :yellow      [213 213 26]
   :aqua        [98 246 153]
   :white       [255 255 254]})

(defonce ^:private panel
  (delay
    (do
      (require 'bocko.swing)
      (let [make-panel (eval 'bocko.swing/make-panel)]
        (make-panel color-map raster width height pixel-width pixel-height)))))

(defn clear
  "Clears this screen."
  []
  (force panel)
  (reset! raster clear-screen)
  nil)

(defonce ^:dynamic
  ^{:doc     "The color used for plotting."}
  *color* default-color)

#(set-validator! #'*color*
                (fn [c] (contains? color-map c)))

(defn- get-current-color
  []
  *color*)

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
  (force panel)
  (set-current-color c)
  nil)

(defn- plot-fn
  [r x y c]
  (assoc-in r [x y] c))

(defn plot
  "Plots a point at a given x and y.

  Both x and y must be between 0 and 39."
  [x y]
  {:pre [(integer? x) (integer? y) (<= 0 x 39) (<= 0 y 39)]}
  (force panel)
  (swap! raster plot-fn x y (get-current-color))
  nil)

(defn- lin
  [r a1 a2 b c f]
  (if (< a2 a1)
    (lin r a2 a1 b c f)
    (reduce (fn [r x]
              (assoc-in r (f [x b]) c))
      r
      (range a1 (inc a2)))))

(defn- hlin-fn
  [r x1 x2 y c]
  (lin r x1 x2 y c identity))

(defn hlin
  "Plots a horizontal line from x1 to x2 at a given y.

  The x and y numbers must be between 0 and 39."
  [x1 x2 y]
  {:pre [(integer? x1) (integer? x2) (integer? y) (<= 0 x1 39) (<= 0 x2 39) (<= 0 y 39)]}
  (force panel)
  (swap! raster hlin-fn x1 x2 y (get-current-color))
  nil)

(defn- vlin-fn
  [r y1 y2 x c]
  (lin r y1 y2 x c reverse))

(defn vlin
  "Plots a vertical line from y1 to y2 at a given x.

  The x and y numbers must be between 0 and 39."
  [y1 y2 x]
  {:pre [(integer? y1) (integer? y2) (integer? x) (<= 0 y1 39) (<= 0 y2 39) (<= 0 x 39)]}
  (force panel)
  (swap! raster vlin-fn y1 y2 x (get-current-color))
  nil)

(defn- scrn-fn
  [r x y]
  (get-in r [x y]))

(defn scrn
  "Gets the color at a given x and y.

  Both x and y must be between 0 and 39."
  [x y]
  {:pre [(integer? x) (integer? y) (<= 0 x 39) (<= 0 y 39)]}
  (force panel)
  (scrn-fn @raster x y))