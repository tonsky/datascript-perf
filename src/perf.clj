(ns perf)

(defmacro later [& body]
 `(js/setTimeout (fn [] ~@body) 0))

(defmacro spy [message & body]
 `(do
    (let [t0#  (util/now)
          res# (do ~@body)
          ms#  (- (util/now) t0#)]
      (js/console.log (util/format-time-str ms# ~message))
      res#)))

(defmacro measure [message & body]
 `(do
    (dotimes [_# util/*warmups*] ~@body) ;; warm-up
    (let [t0# (util/now)
          _#  (dotimes [_# util/*runs*] ~@body)
          t1# (util/now)
          ms# (/ (- t1# t0#) util/*runs*)]
      (js/console.log (util/format-time-str ms# ~message))
      ms#)))
