;;; project-alpha (server-side)
;;;
;;; The use and distribution terms for this software are covered by
;;; the GNU General Public License
;;;
;;; December 2011, Otto Linnemann


(ns project-beta-server.app.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.json-params :as json-params]
            [project-beta-server.local-settings :as setup]
            [compojure.core :as compojure]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [net.cgrand.enlive-html :as html]
            [clojure.tools.nrepl.server :as nrepl-server]
            [cider.nrepl :refer (cider-nrepl-handler)]
                                        ;[swank.swank]
            )
  (:use [clojure.string :only [split]]
        [compojure.core :only [GET POST PUT DELETE]]
        [ring.util.response :only [response content-type charset]]
        [ring.util.codec :only [url-decode url-encode]]
        [ring.middleware.session :only [wrap-session]]
        [ring.middleware.cookies :only [wrap-cookies]]
        [ring.middleware.multipart-params :only [wrap-multipart-params]]
        [ring.middleware.resource]
        [ring.middleware.file-info]
        [clojure.pprint :only [pprint]]
        [clojure.data.json :only [json-str write-json read-json]]
        [macros.macros]))



;; --- application routes ---

(compojure/defroutes main-routes
  ;; --- authentification and registration ---
  ;(GET "/index.html" args (str "<body>" args "</body>"))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> main-routes
      handler/api))


(defn start-server
  "starts the websever"
  []
  (defonce server (jetty/run-jetty #'app
                                   setup/jetty-setup))
  (.start server))



; :configurator remove-non-ssl-connectors

(defn stop-server
  "stop the webserver"
  []
  (.stop server)
  )

(defn -main [& args]
  ;(swank.swank/start-server :port 4005)
  (nrepl-server/start-server :port 7888 :handler cider-nrepl-handler)
  (start-server)
  )

; (start-server)
; (stop-server)
