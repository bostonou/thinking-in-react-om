(defproject om-react "0.1.0-SNAPSHOT"
  :description "Exploring React/Om by rewriting 'Thinking in React' example (http://facebook.github.io/react/blog/2013/11/05/thinking-in-react.html)"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.3.6"]
                 [com.facebook/react "0.8.0.1"]
                 [sablono "0.2.6"]]
  
  :plugins [[lein-cljsbuild "1.0.1"]]

  :source-paths ["src"]

  :cljsbuild { 
    :builds [{:id "om-react"
              :source-paths ["src"]
              :compiler {
                :output-to "om_react.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
