;;;
;;; Clojure based web application
;;; https://github.com/clojure/clojurescript for further information.
;;;
;;; The use and distribution terms for this software are covered by
;;; the GNU General Public License
;;;
;;; ====== main-function ======
;;;

(ns client.app.core
  (:require
   [client.lib.repl :as repl]
   [clojure.browser.dom :as dom]
   [goog.dom :as gdom]
   [goog.events :as events]
   [goog.ui.Button :as Button]
   [goog.ui.ButtonRenderer :as ButtonRenderer]
   [goog.ui.FlatButtonRenderer :as FlatButtonRenderer]
   [goog.style :as style]
   [goog.Timer :as timer])
  (:use [client.app.mandelbrot :only [render-image]]
        [client.lib.logging :only [loginfo]]
        [client.lib.utils :only [current-url-keyword
                                 is-ios-device? show-elements-of-class]]))

(def initial-eval-rect [-2.0 0.5 -1.25 1.25])
(def eval-rect (atom initial-eval-rect))

(def prev-evals-stack (atom (list)))
(defn push-eval-rec [r] (swap! prev-evals-stack conj r))
(defn pull-eval-rec [] (let [t (first @prev-evals-stack)] (if t (swap! prev-evals-stack pop)) t))
(defn clear-eval-rec-stack [] (reset! prev-evals-stack (list)))

(def render-canvas (dom/get-element "render"))
(def render-context (. render-canvas (getContext "2d")))
(def control-canvas (dom/get-element "control"))
(def control-context (. control-canvas (getContext "2d")))
(set! (. control-context -fillStyle) "rgba(255,0,0,0.5)")


(def updateCoordinateTable)

(defn start-rendering
  [canvas [real_min real_max imag_min imag_max]]
  (js/eval "document.body.style.cursor = 'wait';")
  (updateCoordinateTable [real_min real_max imag_min imag_max])
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


;; methods for updating the coordinate table

(defn- setTableCellExp [cell-id value]
  (let [e (dom/get-element cell-id)]
    (set! (. e -innerHTML) (. value (toExponential 8)))))

(defn- updateCoordinateTable [rect]
  (let [[x-min x-max y-min y-max] rect]
    (setTableCellExp"real_min" x-min)
    (setTableCellExp"real_max" x-max)
    (setTableCellExp"imag_min" y-min)
    (setTableCellExp"imag_max" y-max)))

;(updateCoordinateTable [1 2 3 4])

(defn- canvasMouseDownEvent
  [e]
  (let [t (. e -type)
        [x y] (get-evt-coordinates e)]
    (reset! mouse-down-pos [x y])
    (loginfo (str "mouseDownEvent -> x: " x "  ,y: " y))))

(def reset-button)
(def undo-button)

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
    (. control-context (clearRect 0 0 width height))
    (if (or (= x mouse-down-x) (= y mouse-down-y))
      (do
        (reset! mouse-down-pos nil)
        (loginfo "mouseUpEvent but nothing selected"))
      (do
        (push-eval-rec @eval-rect)
        (reset! eval-rect complex-selection-rect)
        (reset! mouse-up-pos [x y])
        (reset! mouse-down-pos nil)
        (loginfo (str "mouseUpEvent: " selection-rect "   ->: " @eval-rect))
        (. reset-button (setEnabled true))
        (. undo-button (setEnabled true))
        (start-rendering render-canvas complex-selection-rect)))))


(defn- canvasMouseMoveEvent
  [e]
  (if @mouse-down-pos
    (let [t (. e -type)
          target (. e -target)
          [width height] [(. target -width) (. target -height)]
          [x y] (get-evt-coordinates e)
          [down-x down-y] @mouse-down-pos
          [x y] (adjust-selection-to-rect-ratio @mouse-down-pos [x y] [width height])
          selection-rect (getMouseSelectionRect @mouse-down-pos [x y])
          complex-selection-rect (get-selected-complex-rect @eval-rect selection-rect [width height])]
      (. control-context (clearRect 0 0 width height))
      (. control-context (fillRect down-x down-y (- x down-x) (- y down-y)))
      (updateCoordinateTable complex-selection-rect)
      (loginfo (str "mouseMoveEvent -> x: " x "  ,y: " y)))))


(events/listen
 control-canvas
 goog.events.EventType.MOUSEDOWN
 canvasMouseDownEvent)

(events/listen
 control-canvas
 goog.events.EventType.MOUSEUP
 canvasMouseUpEvent)

(events/listen
 control-canvas
 goog.events.EventType.MOUSEMOVE
 canvasMouseMoveEvent)


(defn- render-button
  [id call]
  (let [button (goog.ui.decorate (dom/get-element id))]
    (events/listen
     button "action"
     call
     )
    (. button (setEnabled true))
    button))

(def run)
(def reset-button
  (render-button "reset"
                 (fn []
                   (loginfo "reset button clicked")
                   (reset! eval-rect initial-eval-rect)
                   (run "reset"))))


(def undo-button
  (render-button "undo"
                 (fn []
                   (loginfo "undo button clicked")
                   (let [r (pull-eval-rec)]
                     (when r
                       (js/eval "document.body.style.cursor = 'wait';")
                       (when (empty? @prev-evals-stack) (. reset-button (setEnabled false)) (. undo-button (setEnabled false)))
                       (js/eval "document.body.style.cursor = 'wait';")
                       (loginfo (str "undo stack: " @prev-evals-stack))
                       (reset! eval-rect r)
                       (start-rendering render-canvas r))))))

(defn- parse-url-args []
  (let [u (js/eval (str "window.location.href"))
        arg-url (last (first (re-seq #"[?](.*)" u)))]
    (when arg-url
      (let [args (rest (first (re-seq #"(.*)&(.*)&(.*)&(.*)" arg-url)))
        pairs (map #(rest (first (re-seq #"(.*)=(.*)" %))) args)
        hm (reduce #(assoc %1 (first %2) (second %2)) {} pairs)]
        hm))))

(defn- get-url-complex-rect []
  (let [hm (parse-url-args)]
    (when hm
      [(js/parseFloat (hm "x_min")) (js/parseFloat (hm "x_max")) (js/parseFloat (hm "y_min")) (js/parseFloat (hm "y_max"))])))


;; start the rendering process deferred in order to update the UI properly
(defn- run [e]
  (js/eval "document.body.style.cursor = 'wait';")
  (. reset-button (setEnabled false))
  (. undo-button (setEnabled false))
  (clear-eval-rec-stack)
  (let [url-rect (get-url-complex-rect)]
    (if url-rect
      (reset! eval-rect url-rect)
      (reset! eval-rect initial-eval-rect)))
  (start-rendering render-canvas @eval-rect))

(events/listen
 (js/eval "window")
 goog.events.EventType.LOAD
 run
)
