;;;
;;; Clojure based web application
;;; https://github.com/clojure/clojurescript for further information.
;;;
;;; The use and distribution terms for this software are covered by
;;; the GNU General Public License
;;;
;;; ====== main-function ======
;;;

(ns client.app.mandelbrot
  (:use [client.lib.logging :only [loginfo]]))


(defn square [x] (* x x))

(defn cplan2canvas [c_real_min c_real_max c_imag_min c_imag_max c_real c_imag canvas_width canvas_height]
  (let [c_width (- c_real_max c_real_min)
        c_height (- c_imag_max c_imag_min)
        scale_x (/ canvas_width c_width)
        scale_y (/ canvas_height c_height)]
    [(* scale_x (- c_real c_real_min))
     (* scale_y (- c_imag c_imag_min))]))

(defn square_z_plus_c [z_real z_imag c_real c_imag ]
  [(+ (- (square z_real) (square z_imag)) c_real)
   (+ (* 2 z_real z_imag) c_imag)])

(defn c2iterations [c_real c_imag max_iterations]
  (let [iter_z (fn [z_real z_imag iteration]
                 (let [[z_real z_imag] (square_z_plus_c z_real z_imag c_real c_imag)
                       square_z (+ (square z_real) (square z_imag))]
                   (if (or (> square_z 4) (>= iteration max_iterations))
                     iteration
                     (recur z_real z_imag (inc iteration)))))]
    (iter_z 0 0 0)))

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


(defn plot [canvas x y size_x size_y red green blue]
  (let [context (. canvas (getContext "2d"))
        ch (. canvas -height)
        inv_y (- ch y)
        style (str "rgb(" red "," green "," blue")")]
    (set! (. context -fillStyle) style)
    (. context (fillRect x inv_y size_x size_y))))


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
