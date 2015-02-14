(ns util
  (:require
    [clojure.string :as str]
    [cljs.reader]
    [cognitect.transit :as transit]
    [datascript :as d])
  (:import
    goog.net.XhrIo))


(defn ajax [method url & [callback]]
  (println "Fetching" url "...")
  (let [id (str "ajax " method " " url)]
    (js/console.time id)
    (.send goog.net.XhrIo url
      (fn [reply]
        (when callback
          (callback (.. reply -target getResponseText)))
        (js/console.timeEnd id))
      (str/upper-case (name method)))))

(defn ajax-edn [method url callback]
  (ajax method url #(callback (cljs.reader/read-string %))))

(def transit-reader
  (transit/reader :json { :handlers
    { "datascript/Datom" d/datom-from-reader }}))

(defn ajax-transit [method url callback]
  (ajax method url #(callback (transit/read transit-reader %))))

(defn now [] (.getTime (js/Date.)))

(defn permutations [xs]
  (if-not (seq xs)
    [[]]
    (for [x xs
          :let [ys (filter #(not= % x) xs)]
          y (permutations ys)]
      (concat [x] y))))
