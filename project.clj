(defproject datascript-perf "0.1.0"

  :dependencies [
    [org.clojure/clojure "1.7.0-alpha5"]
    [org.clojure/clojurescript "0.0-2850"]
    [datascript "0.9.0"]
    [com.cognitect/transit-cljs "0.8.205"]
  ]

  :plugins [
    [lein-cljsbuild "1.0.4"]
  ]

  :cljsbuild { 
    :builds [
      #_{ :id "none"
        :source-paths  ["src"]
        :compiler {
          :main          main
          :output-to     "perf.js"
          :output-dir    "target/cljs"
          :optimizations :none
          :source-map    true
          :warnings     {:single-segment-namespace false}
      }}
      { :id "advanced"
        :source-paths  ["src"]
        :compiler {
          :output-to     "perf.js"
          :optimizations :advanced
          :pretty-print  false
          :elide-asserts true
          :warnings     {:single-segment-namespace false}
      }}
  ]}
  
  :clean-targets ^{:protect false} [
    "target"
    "perf.js"
  ]
)
