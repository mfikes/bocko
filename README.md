# Bocko

A small library making it extremely simple to play around with low-res graphics from Clojure. (As well as from ClojureScript on [iOS](https://github.com/mfikes/bocko-ios) and an [HTML canvas](https://github.com/mfikes/bocko-canvas)).

<img src="bocko.jpg" width="400px" hspace="5px"/>

# Usage

```
lein new bocko my-project
```

Then find a short README.md at the top-level of the generated project, explaining how to get up and running.

[![Clojars Project](http://clojars.org/bocko/latest-version.svg)](http://clojars.org/bocko)

```clojure
(require '[bocko.core :refer :all])

(plot 2 3)      ;; plots a point on the screen

(color :pink)   ;; changes the color to pink
(plot 5 5)

(scrn 5 5)      ;; => :pink

(hlin 3 9 10)   ;; draws a horizontal line

(clear)         ;; clears screen
```

The commands comprise `color`, `plot`, `scrn`, `hlin`, `vlin`, and `clear`.

# Demo

Watch a demo to see it in action:

[![Bocko Demo](http://img.youtube.com/vi/piJPrP3BKIk/0.jpg)](http://www.youtube.com/watch?v=piJPrP3BKIk "Bocko Clojure simple graphics")

# Examples

Draw an American flag:
```clojure
;; Draw 13 stripes cycling over red/white

(doseq [[n c] (take 13 
                (map vector (range) (cycle [:red :white])))] 
  (color c)
  (let [x1 10 
        x2 25 
        y (+ 10 n)]
    (hlin x1 x2 y)))

;; Fill in a dark blue field in the corner

(color :dark-blue)
(doseq [x (range 10 19)
        y (range 10 17)]
  (plot x y))

;; Add some stars to the field by skipping by 2

(color :white)
(doseq [x (range 11 19 2)
        y (range 11 17 2)]
  (plot x y))
```

Display all the colors:

```clojure
(doseq [[c n] (map vector
                   [:black        :red        :dark-blue    :purple
                    :dark-green   :dark-gray  :medium-blue  :light-blue
                    :brown        :orange     :light-gray   :pink
                    :light-green  :yellow     :aqua         :white]
                   (range))]
  (color c)
  (let [x' (* 10 (rem n 4))
        y' (* 10 (quot n 4))]
    (doseq [x (range x' (+ 10 x'))
            y (range y' (+ 10 y'))]
      (plot x y))))
```


Animated bouncing ball using `loop`/`recur`:
```clojure
(loop [x 5 y 23 vx 1 vy 1]
  ; First determine new location and velocity,
  ; reversing direction if bouncing off edge.
  (let [x' (+ x vx)
        y' (+ y vy)
        vx' (if (< 0 x' 39) vx (- vx))
        vy' (if (< 0 y' 39) vy (- vy))]
    ; Erase drawing at previous location
    (color :black)
    (plot x y)
    ; Draw ball in new location
    (color :dark-blue)
    (plot x' y')
    ; Sleep a little and then loop around again
    (Thread/sleep 50)
    (recur x' y' vx' vy')))
```

Random colors and locations:
```clojure
(loop []
  (let [c (rand-nth [:black        :red        :dark-blue    :purple
                     :dark-green   :dark-gray  :medium-blue  :light-blue
                     :brown        :orange     :light-gray   :pink
                     :light-green  :yellow     :aqua         :white])
        x (rand-int 40)
        y (rand-int 40)]
    (color c)
    (plot x y)
    (Thread/sleep 1)
    (recur)))
```

Game-of-Life Glider:   (Credit: [Christophe Grand](http://clj-me.cgrand.net/2011/08/19/conways-game-of-life/))
```
(defn neighbours [[x y]]
  (for [dx [-1 0 1] dy (if (zero? dx) [-1 1] [-1 0 1])]
    [(+ dx x) (+ dy y)]))

(defn step [cells]
  (set (for [[loc n] (frequencies (mapcat neighbours cells))
             :when (or (= n 3) (and (= n 2) (cells loc)))]
         loc)))

(loop [board #{[0 2] [1 0] [1 2] [2 1] [2 2]}]
  (clear)
  (run! (partial apply plot) board)
  (Thread/sleep 100)
  (recur (step board)))
```

# Multi-threading

You can use Bocko from multiple threads. The underlying canvas is thread-safe.

In that scenario, establishing thread-local bindings for the `*color*` dynamic var will allow each thread to plot independently. For example, the following code will not interfere with the color being used for plotting in other threads:

```clojure
(future
  (binding [*color* :orange]
    (plot 3 3)                ; Will plot an orange point
    (set! *color* :aqua)
    (plot 4 4)))              ; Will plot an aqua point
```

In fact, a form like `(color :red)` is just a simple wrapper that will set a thread-local binding if one is in effect, otherwise it will set the root binding.

Here is an example. This makes use of thread-local bindings and "does the right thing":

```clojure
(do

  ;; Repeatedly display all the colors

  (future
    (loop []
      (clear)
      (doseq [[c n] (map vector
                      [:black :red :dark-blue :purple
                       :dark-green :dark-gray :medium-blue :light-blue
                       :brown :orange :light-gray :pink
                       :light-green :yellow :aqua :white]
                      (range))]
        (binding [*color* c]
          (let [x' (* 10 (rem n 4))
                y' (* 10 (quot n 4))]
            (doseq [x (range x' (+ 10 x'))
                    y (range y' (+ 10 y'))]
              (plot x y)
              (Thread/sleep 1)))))
      (recur)))

  ;; Add a bouncing ball

  (future
    (loop [x 5 y 23 vx 1 vy 1]
      ; First determine new location and velocity,
      ; reversing direction if bouncing off edge.
      (let [x' (+ x vx)
            y' (+ y vy)
            vx' (if (< 0 x' 39) vx (- vx))
            vy' (if (< 0 y' 39) vy (- vy))]
        ; Erase drawing at previous location
        (binding [*color* :black]
          (plot x y))
        ; Draw ball in new location
        (binding [*color* :dark-blue]
          (plot x' y'))
        ; Sleep a little and then loop around again
        (Thread/sleep 50)
        (recur x' y' vx' vy')))))
```

This, on the other hand, illustrates contention / interference with the color being used for plotting:

```clojure
(do

  ;; Repeatedly display all the colors

  (future
    (loop []
      (clear)
      (doseq [[c n] (map vector
                      [:black :red :dark-blue :purple
                       :dark-green :dark-gray :medium-blue :light-blue
                       :brown :orange :light-gray :pink
                       :light-green :yellow :aqua :white]
                      (range))]
        (color c)
        (let [x' (* 10 (rem n 4))
              y' (* 10 (quot n 4))]
          (doseq [x (range x' (+ 10 x'))
                  y (range y' (+ 10 y'))]
            (plot x y)
            (Thread/sleep 1))))
      (recur)))

  ;; Add a bouncing ball

  (future
    (loop [x 5 y 23 vx 1 vy 1]
      ; First determine new location and velocity,
      ; reversing direction if bouncing off edge.
      (let [x' (+ x vx)
            y' (+ y vy)
            vx' (if (< 0 x' 39) vx (- vx))
            vy' (if (< 0 y' 39) vy (- vy))]
        ; Erase drawing at previous location
        (color :black)
        (plot x y)
        ; Draw ball in new location
        (color :dark-blue)
        (plot x' y')
        ; Sleep a little and then loop around again
        (Thread/sleep 50)
        (recur x' y' vx' vy')))))
```


# License

Distributed under the Eclipse Public License, which is also used by Clojure.
