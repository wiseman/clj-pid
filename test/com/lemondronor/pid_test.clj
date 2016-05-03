(ns com.lemondronor.pid-test
  (:require [clojure.test :refer :all]
            [com.lemondronor.pid :as pid]))

(deftest pid-test
  (testing "basic functionality"
    (let [goal 0]
      (loop [pid (pid/pid
                  {:kp 2
                   :ki 1/30
                   :kd 1/2
                   :set-point goal
                   :bounds [-180 180 -1 1]})
             current 30
             n 0]
        (when (< n 10)
          (let [[pid v] (pid/pid pid current)
                new-current (+ current v)]
            (is (< (Math/abs (- goal new-current))
                   (Math/abs (- goal current))))
            (recur pid new-current (inc n))))))))
