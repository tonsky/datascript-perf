(ns util
  (:require
    [clojure.string :as str]
    [cljs.reader]
    [cognitect.transit :as transit]
    [datascript :as d])
  (:import
    goog.net.XhrIo))

(enable-console-print!)

(defn now [] (js/window.performance.now))

(def ^:dynamic *runs* 50)
(def ^:dynamic *warmups* 50)

(defn smart-round [x]
  (loop [d 1]
    (let [n (js/Number (.toFixed x d))]
      (cond
        (> d 9)  x
        (== 0 n) (recur (inc d))
        :else    (js/Number (.toFixed x (inc d)))))))

(defn format-ms [ms]
  (str (smart-round ms) " ms"))

(defn format-time-str [ms & message]
  (let [ms-str (str (smart-round ms))
        [_ p1 p2] (re-matches #"(-?\d+)(\.\d+)?" ms-str)
        p2      (or p2 ".0")
        prepend (max 0 (- 5 (count p1)))
        append  (max 0 (- 3 (count p2)))
        ms-str  (str (apply str (repeat prepend " "))
                     p1
                     p2 
                     (apply str (repeat append " ")))]
    (apply str "[" ms-str " ms ] " message)))

(defn ajax [method url & [callback]]
  (println "Fetching" url "...")
  (let [t0 (now)]
    (.send goog.net.XhrIo url
      (fn [reply]
        (when callback
          (callback (.. reply -target getResponseText)))
        (js/console.log (format-time-str (- (now) t0) "ajax " method " " url)))
      (str/upper-case (name method)))))

(defn ajax-edn [method url callback]
  (ajax method url #(callback (cljs.reader/read-string %))))

(def transit-reader
  (transit/reader :json { :handlers
    { "datascript/Datom" d/datom-from-reader }}))

(defn ajax-transit [method url callback]
  (ajax method url #(callback (transit/read transit-reader %))))

(defn permutations [xs]
  (if-not (seq xs)
    [[]]
    (for [x xs
          :let [ys (filter #(not= % x) xs)]
          y (permutations ys)]
      (concat [x] y))))

