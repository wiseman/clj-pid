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
hold position while the wind is trying to blow us around.

You can imagine that we're trying to control a small multirotor drone.
In this example, we'll use one PID controller that controls the
drone's pitch--that is, tells it how much to tilt forward or backward.
This will let it move in one axis.

(To control a drone in two-dimensional space, we'd add another PID
controller that would tell us how much to tilt left or right. We could
then add a controller for altitude, and another for heading.)

In the following simple model, we have

* A drone that can tilt forward or backward. We assume the drone's
  acceleration is directly proportional to the amount of tilt, up to a
  maximum acceleration of 3 m/s^2. We use Euler integration to compute
  position from acceleration.

* Wind, which starts with a velocity of 0 m/s and then is modeled by a
  random walk.

* Air resistance. Every 100 ms the drone's speed decreases by a factor
  of 0.99.

At time 0, the drone's position and velocity are both 0, the wind
speed is 0, and the drone's target position is 0.

At time 5000 ms, we change the drone's target position to be 50
meters. At time 20000 ms, we change the drone's target position back
to 0 meters. At 30000 ms, the simulation ends.

```
(require '[com.lemondronor.pid :as pid])
(require '[clojure.string :as string])

(def max-accel 3)                ; m/s^2
(def sim-interval 100)           ; milliseconds
(def tf (/ sim-interval 1000.0)) ; time factor
(def resistance 0.99)

(def wind-speeds (iterate #(+ % (- 0.5 (rand))) 0))

(loop
    [lines []
     time 0            ; milliseconds
     winds wind-speeds ; list of m/s
     pos 0             ; m
     vel 0.0           ; m/s
     set-point 0       ; m
     sum-squared-error 0.0
     controller (pid/pid {:kp 20
                          :ki 0.1
                          :kd 12
                          :set-point set-point
                          :bounds [-180 180 -1 1]
                          :sample-period-ms sim-interval})]
  (let [wind (first winds)
        pos (+ pos (* wind tf))
        controller (pid/update controller time pos)
        output (:output controller)
        accel (pid/clamp output (* max-accel (- tf)) (* max-accel tf))
        vel (+ vel accel)
        pos (+ pos vel)
        line (prn-str time (:set-point controller) pos (* accel 100) wind)]
    (if (< time 30000)
      (recur (conj lines line)
             (+ time sim-interval)
             (rest winds)
             pos
             (* vel resistance)
             (cond (< time 5000) 0 (< time 20000) 50 :default 0)
             (+ sum-squared-error (Math/pow (- set-point pos) 2))
             (assoc controller :set-point set-point))
      (do (spit "sim.dat" (string/join "" lines))
          sum-squared-error))))
```

The above code created a file called "sim.dat" containing data about
the progress of the simulation. We can visualize this data using
gnuplot:

```
$ gnuplot
gnuplot> set mxtics 5
gnuplot> set grid mxtics xtics
gnuplot> plot "wind.dat" using 1:2 with lines title "Setpoint (m)", '' using 1:3 with lines title "Position (m)", '' using 1:4 with lines title "Acceleration (cm/s^2)", '' using 1:5 with lines title "Wind velocity (m/s)"
```

Here's the output. You can see the controller basically works, though
the parameters could probably use some tuning.

![Wind example chart](https://cdn.rawgit.com/wiseman/clj-pid/master/wind-example.svg?raw=true&jjwcachebust=1)

You can see that at time 5000 ms, the drone tilts forward as far as it
can to achieve max acceleration toward the target position. At about
6500 ms, when it's roughly halfway to the target position, it tilts
all the way back, so it can slow down and not overshoot the target by
too much.

Once it settles at the target position, at roughly 9000 ms, you can
see it tilting back and forth, fighting the wind.


## Real world example

Here's a video showing the result of using this library to create 2
PID controllers that were used to control a real drone:

[![Wind example chart](https://cdn.rawgit.com/wiseman/clj-pid/master/video-thumbnail.jpg?raw=true)](https://www.youtube.com/watch?v=jpN-BhmvSi8)


## License

Copyright © 2016, 2017 John Wiseman jjwiseman@gmail.com

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
