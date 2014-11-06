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
            )
  (:use [project-beta-client.lib.logging :only [loginfo]]
        [project-beta-client.lib.utils :only [current-url-keyword
                                               is-ios-device? show-elements-of-class]]))


;;; start the client side application
;(start)

(loginfo "Hello")
