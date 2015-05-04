# Bocko
Simple imperative graphics

```clojure
(require 'bocko.core)
(use 'bocko.core)

(plot 2 3)      ;; plots a point on the screen

(color :pink)   ;; changes the color to pink
(plot 5 5)

(scrn 5 5)      ;; => :pink

(hlin 3 9 10)   ;; draws a horizontal line

(clear)         ;; clears screen
```

[Watch a demo](https://youtu.be/piJPrP3BKIk) to see it in action.

Draw an American flag:
```clojure
;; Draw 13 stripes cycling over red/white
(doseq [[y c]
        (take 13 (map vector
                   (range)
                   (cycle [:red :white])))]
  (color c)
  (hlin 10 25 (+ 10 y)))

;; Draw a dark blue field
(color :dark-blue)
(doseq [y (range 7)]
  (hlin 10 18 (+ 10 y)))

;; Add some stars
(color :white)
(doseq [y (range 11 17 2)
        x (range 11 19 2)]
  (plot x y))
```

Animated bouncing ball using `loop`/`recur`:
```clojure
(loop [x 5 y 23 vx 1 vy 1]
  (let [x' (+ x vx)
        y' (+ y vy)
        vx' (if (zero? (rem x' 40))
              (- vx)
              vx)
        vy' (if (zero? (rem y' 40))
              (- vy)
              vy)]
    (color :black)
    (plot x y)
    (color :dark-blue)
    (plot x' y')
    (Thread/sleep 50)
    (recur x' y' vx' vy')))
```
