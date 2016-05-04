# pid

[![Build Status](https://travis-ci.org/wiseman/clj-pid.png?branch=master)](https://travis-ci.org/wiseman/clj-pid) [![Coverage Status](https://coveralls.io/repos/github/wiseman/clj-pid/badge.svg?branch=master)](https://coveralls.io/github/wiseman/clj-pid?branch=master)

A Clojure/Clojurescript library for PID controllers. I've added some
additional features based on the blog posts at
http://brettbeauregard.com/blog/tag/beginners-pid/:

* Handles sampling time/period. Tuning parameters are specified in
  units of "per second" and are scaled to whatever the
  `:sample-period-ms` is.

* Avoids "derivative kick" when the setpoint is changed.

* Output remains smooth when tuning parameters (Kp, Kd, Ki) are
  changed on the fly.


## Usage

The following example is a simulation of using a PID controller to
hold position while the wind is trying to blow us around:

```
(require '[com.lemondronor.pid :as pid])
(require '[clojure.string :as string])

(loop
    [lines []
     time 0
     wind 0
     pos 10
     controller (pid/pid {:kp 12.5
                          :ki 2
                          :kd 0.4
                          :set-point 30
                          :bounds [-180 180 -1 1]
                          :sample-period-ms 100})]
  (let [pos (+ pos wind)
        [controller output] (pid/pid controller time pos)
        pos (+ pos (* 10 output))
        line (prn-str (:set-point controller) pos output wind)]
    (let [controller (assoc controller :set-point (set-point (int (/ time 80))))]
      (if (< time 15000)
        (recur (conj lines line) (+ time 100) (+ wind (or nil (- 0.5 (rand)))) pos controller)
        (spit "wind.dat" (string/join "" lines))))))
```

From the chart below, you can see the controller basically works,
though the parameters could probably use some tuning:

![Wind example chart](https://cdn.rawgit.com/wiseman/clj-pid/master/wind-example.svg?raw=true)

## License

Copyright © 2016 John Wiseman jjwiseman@gmail.com

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
