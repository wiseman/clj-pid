(ns com.lemondronor.pid-test
  (:require [clojure.test :refer :all]
            [com.lemondronor.pid :as pid]))

(deftest pid-test
  (testing "basic functionality"
    (loop [pid (pid/pid
                {:kp 2
                 :ki 1/30
                 :kd 1/2
                 :set-point 0
                 :bounds [-180 180 -1 1]
                 :period-ms 100})
           current 30
           goal 0
           n 0]
      (when (< n 300)
        (let [pid (if (= n 150)
                    (pid/set-tunings pid 2 1/5 1/2)
                    (assoc pid :set-point goal))
              [pid v] (pid/pid pid (* n 100) current)
              new-current (+ current v)]
          (println n new-current v)
;;          (is (< (Math/abs (- goal new-current))
;;                 (Math/abs (- goal current))))
          (recur pid new-current (if (< n 200) 0 30) (inc n)))))))
