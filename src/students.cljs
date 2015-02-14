(ns students
  (:require-macros
    [perf :refer [measure spy later]])
  (:require
    [clojure.string :as str]
    [datascript :as d]
    [util :refer [ajax-transit now]]))

(enable-console-print!)

(declare fetch-data populate-db run-tests)

(def schema {
  :lesson/teacherid          {:db/valueType :db.type/ref } 
  :lesson/lessongroupid      {:db/valueType :db.type/ref }
  :enrollment/lessongroup_id {:db/valueType :db.type/ref }
  :enrollment/student_id     {:db/valueType :db.type/ref }
  :invoice/enrollment_id     {:db/valueType :db.type/ref }
})

(defn fetch-data []
  (ajax-transit :get "./data-students.transit"
    (fn [data]
      (println "Loaded" (count data) "datoms")
      (later (populate-db schema data)))))

(defn populate-db [schema datoms]
  (spy "populate-db"
    (let [db (d/init-db datoms schema)]
      (println "Added" (count (:eavt db)) "datoms")
      (later (run-tests db)))))

(def lesson-keys [:id :status :starttime :endtime :paid :students :lgid])

(def tid 2001)
(def week 6)

(defn q1 [db]
  (d/q '{:find  [?lid ?status ?starttime ?endtime (min ?paid) (distinct ?studentinfo) ?lgid]
         :in    [$ ?tid ?week ?list]
         :where [[?lid :lesson/teacherid ?tid]
                 [?lid :lesson/week ?week]
                 [?lid :lesson/lessongroupid ?lgid]
                 [?eid :enrollment/lessongroup_id ?lgid]
                 [?eid :enrollment/student_id ?sid]
                 [?iid :invoice/enrollment_id ?eid]
                 [?sid :student/firstname ?fname]
                 [?sid :student/lastname ?lname]
                 [?iid :invoice/paid ?paid]
                 [?lid :lesson/status ?status]
                 [?lid :lesson/starttime ?starttime]
                 [?lid :lesson/endtime ?endtime]
                 [(?list ?sid ?fname ?lname) ?studentinfo]
                 ]}
       db tid week list))


(defn q1-opt [db]
  (let [data (d/q '[:find  ?lid (min ?paid) (distinct ?sid)
                    :in    $ ?tid ?week
                    :where [?lid :lesson/teacherid ?tid]
                           [?lid :lesson/week ?week]
                           [?lid :lesson/lessongroupid ?lgid]
                           [?eid :enrollment/lessongroup_id ?lgid]
                           [?iid :invoice/enrollment_id ?eid]
                           [?iid :invoice/paid ?paid]
                           [?eid :enrollment/student_id ?sid]] db tid week)]
    (for [[lid paid sids] data
          :let [lesson   (d/pull db [:db/id :lesson/lessongroupid :lesson/status :lesson/starttime :lesson/endtime] lid)
                students (mapv #(d/pull db [:db/id :student/firstname :student/lastname] %) sids)]]
      (assoc lesson
        :paid paid
        :students students))))


(defn q-find-starttime-ll [db lgids]
  (d/q '{:find [?lgid (max ?start)]
         :in [$ [?lgid ...]]
         :where [[?lid :lesson/lessongroupid ?lgid]
                 [?lid :lesson/starttime ?start]
                 ]}
       db lgids))

(defn q2 [db] (q-find-starttime-ll db [6028]))

(defn q2-opt [db]
  (for [lgid [6028]
        :let [datoms (d/datoms db :avet :lesson/lessongroupid lgid)]]
    [lgid (reduce max (sequence
                        (comp (map :e)
                              (map #(d/entity db %))
                              (map :lesson/starttime))
                         datoms))]))

(defn run-tests [db]
  (println "Measured q1:" (measure 50 (q1 db)) "ms")
  (println "Measured q1-opt:" (measure 50 (q1-opt db)) "ms")
  (println "Measured q2:" (measure 50 (q2 db)) "ms")
  (println "Measured q2-opt:" (measure 50 (q2-opt db)) "ms"))

(defn ^:export start []
  (fetch-data))
