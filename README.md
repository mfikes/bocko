# Bocko

A small library making it extremely simple to play around with low-res graphics from Clojure.

# Usage

```clojure
(require ['bocko.core :refer :all])

(plot 2 3)      ;; plots a point on the screen

(color :pink)   ;; changes the color to pink
(plot 5 5)

(scrn 5 5)      ;; => :pink

(hlin 3 9 10)   ;; draws a horizontal line

(clear)         ;; clears screen
```

# Demo

Watch a demo to see it in action:

[![Bocko Demo](http://img.youtube.com/vi/piJPrP3BKIk/0.jpg)](http://www.youtube.com/watch?v=piJPrP3BKIk "Bocko Clojure simple graphics")

# Examples

Draw an American flag:
```clojure
;; Draw 13 stripes cycling over red/white

(doseq [[stripe-number stripe-color]
        (take 13
          (map vector
            (range)
            (cycle [:red :white])))]
  (color stripe-color)
  (let [x1 10
        x2 25
        y (+ 10 stripe-number)]
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

# License

Distributed under the Eclipse Public License, which is also used by ClojureScript.
