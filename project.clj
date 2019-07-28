(defproject mandelbrot-set "1.0.0-SNAPSHOT"
  :description "generation of mandelbrot set within web browser"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [ring "1.7.1"]
                 [ring-json-params "0.1.3"]
                 [compojure "0.6.5" :exclusions
                  [org.clojure/clojure org.clojure/clojure-contrib]]
                 [org.ol42/enlive "1.3.0-corfield-alpha1"]
                 [korma "0.3.0-RC2"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [org.clojure/java.jdbc "0.2.2"]
                 [org.clojure/data.json "0.1.1"]
                 [org.apache.commons/commons-email "1.2"]
                 [org.clojure/data.json "0.2.5"]]
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-ring "0.12.5"]]
  :source-paths ["src"]
  :ring {:handler server.app.core/app}
  :cljsbuild {
              :repl-listen-port 9000
              :builds {:release
                       {:source-paths ["src/client"]
                        :compiler {:output-to "resources/public/release.js"
                                   :optimizations :advanced
                                   :pretty-print false}}
                       :debug
                       {:source-paths ["src/client"]
                        :compiler {:output-to "resources/public/debug.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}
                       }}
  :aot [server.app.core]
  :main server.app.core
  :uberjar {:aot :all})
