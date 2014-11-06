;;; project-alpha (server-side)
;;;
;;; The use and distribution terms for this software are covered by
;;; the GNU General Public License
;;;
;;; December 2011, Otto Linnemann

(ns project-beta-server.local-settings)


;;; --- Port and domain setup ---

(def http-port 3000)
(def https-port 3443)
(def host-url (str "https://localhost" (when-not (= https-port 443) (str ":" https-port)) "/"))
(def jetty-setup
  {:port http-port
   :join? false
   :ssl? true
   :ssl-port https-port
   :keystore "resources/keys/key_crt.jks"
   :key-password "password"})

(def cookie-max-age (* 30 24 3600))


(def ^{:doc "when true display maintenance page"} maintencance-mode false)
