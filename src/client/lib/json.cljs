;;; JSON Helpers
;;; taken from: http://mmcgrana.github.com/2011/09/clojurescript-nodejs.html
;;;
;;; Eclipse Public License 1.0

(ns client.lib.json)

(defn my-clj->js
  "Recursively transforms ClojureScript maps into Javascript objects,
   other ClojureScript colls into JavaScript arrays, and ClojureScript
   keywords into JavaScript strings. Note that this function’s approximate
   inverse js->clj is provided by ClojureScript core."
  [x]
  (cond
    (string? x) x
    (keyword? x) (name x)
    (map? x) (. (reduce (fn [m [k v]]
               (assoc m (clj->js k) (clj->js v))) {} x) -strobj)
    (coll? x) (apply array (map clj->js x))
    :else x))

(defn generate
  "Returns a newline-terminate JSON string from the given
   ClojureScript data."
  [data]
  (str (js/JSON.stringify (clj->js data)) "\n"))

(defn parse
  "Returns ClojureScript data for the given JSON string."
  [line]
  (js->clj (js/JSON.parse line)))
