(ns bundles
  (:require-macros
    [perf :refer [measure spy later]])
  (:require
    [clojure.string :as str]
    [datascript :as d]
    [util :refer [ajax-edn now permutations]]))

(enable-console-print!)

(declare query fetch-schema fetch-data populate-db run-tests)

(defn tx->datom [[_ e a v]]
  (d/datom e a v))

(def schema {
  :r/id        { :db/unique      :db.unique/identity }
  :r/owner     { :db/valueType   :db.type/ref }
  :r/connector { :db/valueType   :db.type/ref }
  :r/rules     { :db/cardinality :db.cardinality/many }
  :r/inputs    { :db/valueType   :db.type/ref
                 :db/cardinality :db.cardinality/many }
  :r/outputs   { :db/valueType   :db.type/ref
                 :db/cardinality :db.cardinality/many }
  :c.twitter/queries      { :db/cardinality :db.cardinality/many }
  :c.twitter/users        { :db/cardinality :db.cardinality/many }
  :c.instagram/users      { :db/cardinality :db.cardinality/many }
  :c.instagram/tags       { :db/cardinality :db.cardinality/many }
  :c.streamserver/targets { :db/cardinality :db.cardinality/many }
})

(defn fetch-data []
  (ajax-edn :get "./data-bundles.tx"
    (fn [data]
      (println "Loaded" (count data) "datoms")
      (later (populate-db data)))))

(defn populate-db [data]
  (spy "populate-db"
    (let [db (d/init-db (map tx->datom data) schema)]
    (println "Added" (count (:eavt db)) "datoms")
    (later (run-tests db)))))

(def q '[[?bundle :r/outputs ?output]
         [?output :r/state   :enabled]
         [?bundle :r/inputs  ?input]
         [?input  :r/state   :enabled]])

(defn query [q db]
  (d/q (concat '[ :find ?input
                  :where ] q)
       db))

(defn run-tests [db]
  (println "Measured query:" (measure 50 (query q db)) "ms" q))

(defn ^:export start []
  (fetch-data))
