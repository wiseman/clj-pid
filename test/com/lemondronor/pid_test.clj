(ns com.lemondronor.pid-test
  (:require [clojure.test :refer :all]
            [com.lemondronor.pid :as pid]))


(deftest scale-tests
  (testing "scale"
    (is (= 1 (pid/scale 1 0 1 0 1)))
    (is (= 1/2 (pid/scale 1 0 2 0 1)))))


(deftest pid-test
  (testing "basic functionality"
    (let [controller (pid/pid
                      {:kp 2
                       :ki 1/30
                       :kd 1/2
                       :set-point 0
                       :bounds [-180 180 -1 1]
                       :period-ms 100})
          current 30
          goal 0
          n 0])))
