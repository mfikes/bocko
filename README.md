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
