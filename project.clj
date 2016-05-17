(defproject com.lemondronor/pid "4.0.6"
  :description "PID controller in Clojure. Code for writing PID control loops."
  :url "http://github.com/wiseman/clj-pid"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :deploy-repositories [["releases" :clojars]]
  :source-paths ["src/cljc"]
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :profiles {:dev {:plugins [[lein-cloverage "1.0.6"]
                             [lein-codox "0.9.4"]]}})
