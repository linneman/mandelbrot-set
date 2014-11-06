(defproject project-beta "1.0.0-SNAPSHOT"
  :description "clojure based web application"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring/ring-core "1.1.0"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [ring-json-params "0.1.3"]
                 [compojure "0.6.5" :exclusions
                  [org.clojure/clojure org.clojure/clojure-contrib]]
                 [org.ol42/enlive "1.3.0-corfield-alpha1"]
                 [korma "0.3.0-RC2"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [org.clojure/java.jdbc "0.2.2"]
                 [org.clojure/data.json "0.1.1"]
                 [org.apache.commons/commons-email "1.2"]
                 [org.clojure/data.json "0.2.5"]
                 [org.clojure/clojurescript "0.0-2371"]
                 ]
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-ring "0.7.0"]]
  :source-paths ["src"]
  ;; :hooks [leiningen.cljsbuild]
  :cljsbuild {
              :repl-listen-port 9000
              :builds {:release
                       {:source-paths ["src/project_beta_client"]
                        :compiler {:output-to "resources/public/project_beta.js"
                                   :optimizations :advanced
                                   :pretty-print false}}
                       :debug
                       {:source-paths ["src/project_beta_client"]
                        :compiler {:output-to "resources/public/project_beta_debug.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}
                       }}
  :main project-beta-server.app.core)
