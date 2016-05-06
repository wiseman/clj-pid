(ns com.lemondronor.pid-test
  (:require [clojure.test :refer :all]
            [com.lemondronor.pid :as pid]))


(deftest scale-tests
  (testing "scale"
    (is (= 1 (pid/scale 1 0 1 0 1)))
    (is (= 1/2 (pid/scale 1 0 2 0 1)))))


(deftest clamp-tests
  (testing "clamp"
    (is (= 0 (pid/clamp -10 0 1)))
    (is (= 1 (pid/clamp 10 0 1)))
    (is (= 1/2 (pid/clamp 1/2 0 1)))))


(defn a= [a b]
  (let [eps 0.000001]
    (< (Math/abs (- a b)) eps)))


(deftest set-coefficients-tests
  (testing "set-coefficients"
    (let [c (->
             (pid/pid {:kp 8
                       :ki 4
                       :kd 2
                       :set-point 0
                       :bounds [-180 180 -1 1]
                       :sample-period-ms 1000})
             (pid/set-coefficients 8 4 2))]
      (is (a= 8.0 (:kp c)))
      (is (a= 4.0 (:ki c)))
      (is (a= 2.0 (:kd c))))
    (let [c (->
             (pid/pid {:kp 8
                       :ki 4
                       :kd 2
                       :set-point 0
                       :bounds [-180 180 -1 1]
                       :sample-period-ms 100})
             (pid/set-coefficients 8 4 2))]
      (is (a= 8 (:kp c)))
      (is (a= 0.4 (:ki c)))
      (is (a= 20.0 (:kd c))))))


(deftest set-sample-rate
  (testing "set-sample-rate"
    (let [c (->
             (pid/pid {:kp 8
                       :ki 4
                       :kd 2
                       :set-point 0
                       :bounds [-180 180 -1 1]
                       :sample-period-ms 1000})
             (pid/set-sample-rate 10))]
      (is (a= 100 (:sample-period-ms c)))
      (is (a= 8.0 (:kp c)))
      (is (a= 0.4 (:ki c)))
      (is (a= 20.0 (:kd c))))))


(deftest pid-test
  (testing "basic functionality"
    (loop [pid (pid/pid
                {:kp 0.1
                 :ki 1/30
                 :kd 1/200
                 :set-point 0
                 :bounds [-180 180 -1 1]
                 :sample-period-ms 1})
           time 0
           pos 100
           prev-pos nil]
      (when (< time 10)
        (when prev-pos
          (is (< pos prev-pos)))
        (let [pid (pid/update pid time pos)]
          (recur pid
                 (inc time)
                 (+ pos (:output pid))
                 pos))))))
