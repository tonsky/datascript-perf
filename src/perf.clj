(ns perf)

(defmacro later [& body]
 `(js/setTimeout (fn [] ~@body) 0))

(defmacro spy [message & body]
 `(do
    (js/console.time ~message)
    (let [res# (do ~@body)]
      (js/console.timeEnd ~message)
      res#)))

(defmacro measure [times & body]
 `(do
    (dotimes [_# 50]
      ~@body) ;; warm-up
    (let [t0# (util/now)]
      (dotimes [_# ~times]
        ~@body)
      (/ (- (util/now) t0#) ~times))))
