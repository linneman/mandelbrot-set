;;;
;;; Clojure based web application
;;; https://github.com/clojure/clojurescript for further information.
;;;
;;; The use and distribution terms for this software are covered by
;;; the GNU General Public License
;;;
;;; ====== utility functions ======
;;;
;;; 2011-11-23, Otto Linnemann


(ns client.lib.utils
  (:require [client.lib.json :as json]
            [clojure.browser.event :as event]
            [clojure.browser.dom   :as dom]
            [goog.dom :as gdom]
            [goog.ui.Dialog :as Dialog]
            [goog.ui.Button :as Button]
            [goog.ui.FlatButtonRenderer :as FlatButtonRenderer]
            [goog.net.XhrIo :as ajax]
            [goog.events :as events]
            [goog.events.EventType :as event-type]
            [goog.style :as style])
  (:use [client.lib.logging :only [loginfo]]))


(defn- current-url-keyword
  "returns the keyword matching to the currently opened URL (page)
   This allows to select the page the client renders by the URL.
   In example for http://project-alpha/index.html we get the
   symbol :index which per definition is used used for the client
   side rendering via switch-to-page function."
  []
  (let [[page-html page]
        (first (re-seq #"([a-zA-Z_]*)\.html$" (js* "document.URL")))]
    (keyword page)))


(defn parse-url-args
  "returns a hash-map providing the arguments of an http get request"
  []
  (let [u (js/eval (str "window.location.href"))
        arg-url (last (first (re-seq #"[?](.*)" u)))]
    (when arg-url
      (let [args (rest (first (re-seq #"(.*)&(.*)&(.*)&(.*)" arg-url)))
        pairs (map #(rest (first (re-seq #"(.*)=(.*)" %))) args)
        hm (reduce #(assoc %1 (first %2) (second %2)) {} pairs)]
        hm))))


(defn rewrite-url
  "rewrites the url to given string"
  [url]
  (loginfo (str "url rewritten to: " url))
  (js/eval (str "window.history.pushState('', 'Mandelbrot set', '" url "');")))


(defn get-element
  "similar to dom/get-element but the search can be
   restricted to a given node (2nd argument) If no
   node is specified the document object is searched."
  ([element] (get-element element (gdom/getDocument)))
  ([element node]
      (gdom/findNode node
                     (fn [e] (= (. e -id) element)))))



(defn copy-id-text
  "copies the text which is refered by given HTML id
       string to the 'to-id-str' HTML id."
  [from-id-str to-id-str]
  (let [from-elem (dom/get-element from-id-str)
        to-elem (dom/get-element to-id-str)]
    (goog.dom.setTextContent to-elem (goog.dom.getTextContent from-elem))))


(defn clear-id-text
  "clears the given HTML id element text"
  [id-str]
  (let [elem (dom/get-element id-str)]
    (goog.dom.setTextContent elem " ")))

(comment
  usage illustration

  (copy-id-text "name_not_available_error" "register_message_name")
  (clear-id-text "register_message_name" ""))



(defn is-ios-device?
  "true on ipad, phone, ipod"
  []
  (js/eval "navigator.userAgent.match(/(iPad|iPhone|iPod)/i) ? true : false"))


(defn show-elements-of-class
  "show (normally hidden) elements of given class"
  [class]
  (let [ios-nodes (gdom/findNodes (gdom/getDocument)
                                  (fn [e] (= (. e -className) class)))]
    (dorun (map #(set! (. (. % -style) -display) "inline") ios-nodes))))
