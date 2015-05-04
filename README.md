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

Bouncing ball using `loop`/`recur`:
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

[Watch a demo](https://youtu.be/piJPrP3BKIk) to see it in action.
