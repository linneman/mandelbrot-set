;;;
;;; Clojure based web application
;;; https://github.com/clojure/clojurescript for further information.
;;;
;;; The use and distribution terms for this software are covered by
;;; the GNU General Public License
;;;
;;; ====== main-function ======
;;;
;;; 2011-11-23, Otto Linnemann


(ns project-beta-client.app.core
  (:require
            [project-beta-client.lib.repl :as repl]
            [clojure.browser.dom :as dom]
            [goog.dom :as gdom]
            [goog.events :as events]
            [goog.style :as style]
            [goog.Timer :as timer])
  (:use [project-beta-client.lib.logging :only [loginfo]]
        [project-beta-client.lib.utils :only [current-url-keyword
                                               is-ios-device? show-elements-of-class]]))

(def eval-rect (atom [-2.0 0.5 -1.25 1.25]))
(def eps-min 1e-10)

(def render-canvas (dom/get-element "render"))
(def render-context (. render-canvas (getContext "2d")))
(def control-canvas (dom/get-element "control"))
(def control-context (. control-canvas (getContext "2d")))
(set! (. control-context -fillStyle) "rgba(255,0,0,0.5)")

; (. render-context (fillRect 50 25 150 100))

(comment
  (. render-context -fillStyle)
  (set! (. render-context -fillStyle) "#ccc")
  (set! (. render-context -fillStyle) "rgb(255,0,0)")
  (. render-context (fillRect 50 50 150 100))
  (set! (. render-context -fillStyle) "rgb(0,255,0)")
  (. render-context (fillRect 100 50 150 100))

  (. render-canvas -width)
  (. render-canvas -height)

  (str "rgb(" 255 "," 0 "," 0")")
  )


(defn plot [canvas x y size_x size_y red green blue]
  (let [context (. canvas (getContext "2d"))
        ch (. canvas -height)
        inv_y (- ch y)
        style (str "rgb(" red "," green "," blue")")]
    (set! (. context -fillStyle) style)
    (. context (fillRect x inv_y size_x size_y))))


(defn square [x] (* x x))

(defn cplan2canvas [c_real_min c_real_max c_imag_min c_imag_max c_real c_imag canvas_width canvas_height]
  (let [c_width (- c_real_max c_real_min)
        c_height (- c_imag_max c_imag_min)
        scale_x (/ canvas_width c_width)
        scale_y (/ canvas_height c_height)]
    [(* scale_x (- c_real c_real_min))
     (* scale_y (- c_imag c_imag_min))]))

(comment
  (cplan2canvas -2.0 0.5 -1.25 1.25   0.5 1.25   500 500)
  (cplan2canvas -2.0 0.5 -1.25 1.25   -2.0 -1.25   500 500)
  (plot render-canvas 2 2 10 10 0 0 100)
  (plot render-canvas 3 3 10 10 0 0 100))

(defn square_z_plus_c [z_real z_imag c_real c_imag ]
  [(+ (- (square z_real) (square z_imag)) c_real)
   (+ (* 2 z_real z_imag) c_imag)
   c_real
   c_imag])

(defn c2iterations [c_real c_imag max_iterations]
  (let [iter_z (fn [z_real z_imag c_real c_imag iteration]
                 (let [[z_real z_imag c_real c_imag] (square_z_plus_c z_real z_imag c_real c_imag)
                       square_z (+ (square z_real) (square z_imag))]
                   (if (or (> square_z 4) (>= iteration max_iterations))
                     iteration
                     (recur z_real z_imag c_real c_imag (inc iteration)))))]
    (iter_z 0 0 c_real c_imag 0)))

; (c2iterations -1.25 0.1 100)

(defn linear_color_density_map [iteration max_iterations]
  (let [iteration (min iteration max_iterations)
        x (* 2 (- (/ iteration max_iterations) 0.5))
        y (- 1 (square x))]
    y))

(defn exp_color_density_map [iteration max_iterations]
  (let [iteration (min iteration max_iterations)
        x (* 2 (- (/ iteration max_iterations) 0.5))
        y (Math/exp (* -10 (square x)))]
    y))

(def color_density_map exp_color_density_map)

; (color_density_map 101 100)

(defn iteration2rgb [iteration max_iterations]
  (let [max_intensity (- (* 256 256 256) 1)
        scale (/ max_intensity max_iterations)
        iteration (color_density_map iteration max_iterations)
        intensity (int (* scale iteration))
        red (bit-and 255 (bit-shift-right intensity 16))
        green (bit-and 255 (bit-shift-right intensity 8))
        blue (bit-and 255 intensity)]
    [red green blue]))

; (iteration2rgb 4 100)


(defn render-image [canvas real_min real_max imag_min imag_max size_x size_y max_iterations]
  (let [canvas_width (. canvas -width)
        canvas_height (. canvas -height)
        real_steps (/ canvas_width size_x)
        imag_steps (/ canvas_height size_y)
        real_range (range real_min real_max (/ (- real_max real_min) real_steps))
        imag_range (range imag_min imag_max (/ (- imag_max imag_min) imag_steps))]
    (dorun
     (for [real real_range imag imag_range]
       (let [[x y] (cplan2canvas real_min real_max imag_min imag_max real imag canvas_width canvas_height)
             [red green blue] (iteration2rgb (c2iterations real imag max_iterations) max_iterations)]
         ;(println x y "->" red green blue)
         (plot canvas x y size_x size_y red green blue))))))

;(render-image render-canvas -2.0 0.5 -1.25 1.25 1 1 100)


(defn start-rendering
  [canvas [real_min real_max imag_min imag_max]]
  (js/eval "document.body.style.cursor = 'wait';")
  (timer/callOnce #(do
                     (render-image render-canvas real_min real_max imag_min imag_max 10 10 100)) 10)
  (timer/callOnce #(do
                     (render-image render-canvas real_min real_max imag_min imag_max 1 1 100)
                     (js/eval "document.body.style.cursor = 'default';")) 100))


(defn- get-evt-coordinates [e]
  (let [target (. e -target)
        rect (. target (getBoundingClientRect))
        x (- (. e -clientX) (. rect -left))
        y (- (. e -clientY) (. rect -top))]
        [x y]
        ))


(def mouse-down-pos (atom nil))
(def mouse-up-pos (atom [(. render-canvas -width) (. render-canvas -height)]))


(defn- canvasMouseEvent
  [e]
  (let [t (. e -type)
        [x y] (get-evt-coordinates e)]
    (case t
      "mousedown"
      (do
        (loginfo (str "mouseDownEvent -> x: " x "  ,y: " y)))
      "mouseup"
      (do
        (loginfo (str "mouseUpEvent -> x: " x "  ,y: " y)))
      "mousemove" (loginfo (str "mouseMoveEvent -> x: " x "  ,y: " y)))))


(defn- getMouseSelectionRect [down-pos up-pos]
  (let [[x-down y-down] down-pos
        [x-up y-up] up-pos
        [x-min x-max] (if (< x-down x-up) [x-down x-up] [x-up x-down])
        [y-min y-max] (if (< y-down y-up) [y-down y-up] [y-up y-down])]
    [x-min x-max y-min y-max]))

(defn- sign [x] (if (= x 0) 0 (/ x (Math/abs x))))

(defn- adjust-selection-to-rect-ratio [mouse-down-pos mouse-up-pos width-height]
  (let [[down-x down-y] mouse-down-pos
        [up-x up-y] mouse-up-pos
        [width height] width-height
        [sel-width sel-height] [(Math/abs (- up-x down-x)) (Math/abs (- up-y down-y))]]
    (if (> sel-width sel-height)
      [up-x (+ down-y (* (sign (- up-y down-y )) sel-width (/ width height)))]
      [(+ down-x (* (sign (- up-x down-x)) sel-height (/ height width))) up-y])))

;(adjust-selection-to-rect-ratio [10 20] [80 40] [100 100])


(defn- get-selected-complex-rect [base-rect selection-rect canvas-width-height]
  (let [[width height] canvas-width-height
        [x-min x-max y-min y-max] base-rect
        [pane-x-min pane-x-max pane-y-min pane-y-max] selection-rect
        [pane-y-min pane-y-max] [(- height pane-y-max) (- height pane-y-min)]
        x-min-ratio (/ pane-x-min width)
        x-max-ratio (/ pane-x-max width)
        y-min-ratio (/ pane-y-min height)
        y-max-ratio (/ pane-y-max height)]
    [(+ x-min (* x-min-ratio (- x-max x-min)))
     (+ x-min (* x-max-ratio (- x-max x-min)))
     (+ y-min (* y-min-ratio (- y-max y-min)))
     (+ y-min (* y-max-ratio (- y-max y-min)))]))


(defn- canvasMouseDownEvent
  [e]
  (let [t (. e -type)
        [x y] (get-evt-coordinates e)]
    (reset! mouse-down-pos [x y])
    (loginfo (str "mouseDownEvent -> x: " x "  ,y: " y))))

(defn- canvasMouseUpEvent
  [e]
  (let [t (. e -type)
        target (. e -target)
        [width height] [(. target -width) (. target -height)]
        [mouse-down-x mouse-down-y] @mouse-down-pos
        [x y] (get-evt-coordinates e)
        [x y] (adjust-selection-to-rect-ratio @mouse-down-pos [x y] [width height])
        selection-rect (getMouseSelectionRect @mouse-down-pos [x y])
        complex-selection-rect (get-selected-complex-rect @eval-rect selection-rect [width height])]
    (if (or (= x mouse-down-x) (= y mouse-down-y))
      (loginfo "mouseUpEvent but nothing selected")
      (do
        (. control-context (clearRect 0 0 width height))
        (reset! eval-rect complex-selection-rect)
        (reset! mouse-up-pos [x y])
        (reset! mouse-down-pos nil)
        (loginfo (str "mouseUpEvent: " selection-rect "   ->: " @eval-rect))
        (start-rendering render-canvas @eval-rect)))))


(defn- canvasMouseMoveEvent
  [e]
  (if @mouse-down-pos
    (let [t (. e -type)
          target (. e -target)
          [width height] [(. target -width) (. target -height)]
          [x y] (get-evt-coordinates e)
          [down-x down-y] @mouse-down-pos
          [x y] (adjust-selection-to-rect-ratio @mouse-down-pos [x y] [width height])]
      (. control-context (clearRect 0 0 width height))
      (. control-context (fillRect down-x down-y (- x down-x) (- y down-y)))
      (loginfo (str "mouseMoveEvent -> x: " x "  ,y: " y)))))


(events/listen
 control-canvas
 (clj->js goog.events.EventType.MOUSEDOWN)
 canvasMouseDownEvent)

(events/listen
 control-canvas
 (clj->js goog.events.EventType.MOUSEUP)
 canvasMouseUpEvent)

(events/listen
 control-canvas
 (clj->js goog.events.EventType.MOUSEMOVE)
 canvasMouseMoveEvent)


;; start the rendering process deferred in order to update the UI properly
(defn- run [e]
  (js/eval "document.body.style.cursor = 'wait';")
  (start-rendering render-canvas @eval-rect))

(events/listen
 (js/eval "window")
 goog.events.EventType.LOAD
 run
)
