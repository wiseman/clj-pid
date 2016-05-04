(ns com.lemondronor.pid)


(defn scale
  "Maps a value `x` from the input domain of [`in-min`, `in-max`] to
  the output range of [`out-min`, `out-max`]."
  [x in-min in-max out-min out-max]
  (+ (/ (* (- x in-min) (- out-max out-min)) (- in-max in-min)) out-min))


(defn clamp
  "Clamps a value `x` to the range [`min`, `max`] such that the return
  value is always within that range."
  [x min max]
  (cond
    (> x max) max
    (< x min) min
    :default x))


(defn set-coefficients
  "Sets the Kp, Ki, and Kd coefficients of a PID controller `c`.
  Coefficients should be specified in units of \"per second\", and
  will be scaled for the controller's actual sample rate."
  [c kp ki kd]
  (let [sample-period-s (/ (:sample-period-ms c) 1000.0)]
    (assoc c
           :kp kp
           :ki (* ki sample-period-s)
           :kd (/ kd sample-period-s))))


(defn set-sample-sample-period
  "Sets a PID controller `c`'s sample sample-period, in millseconds."
  [c sample-period-ms]
  (let [ratio (/ sample-period-ms (:sample-period-ms c))]
    (assoc c
           :ki (* (:ki c) ratio)
           :kd (/ (:kd c) ratio)
           :sample-period-ms sample-period-ms)))


(defn set-sample-rate
  "Sets a PID controller `c`'s sample rate, in Hz."
  [c rate]
  (set-sample-sample-period c (/ 1000.0 rate)))


(defn pid
  "Creates a PID controller."
  [c]
  (-> c
      (assoc :i-term 0 :last-input 0 :last-time-ms nil :last-value 0)
      (set-coefficients (:kp c) (:ki c) (:kd c))))

(defn update
  "Updates a PID controller."
  [c time-ms value]
  (if (or (nil? (:last-time-ms c))
          (>= (- time-ms (:last-time-ms c)) (:sample-period-ms c)))
    (let [{:keys [set-point kp kd ki i-term last-input bounds]} c
          [in-min in-max out-min out-max] bounds
          value (scale (clamp value in-min in-max) in-min in-max -1.0 1.0)
          sp (scale (clamp set-point in-min in-max) in-min in-max -1.0 1.0)
          error (- sp value)
          p-val (* kp error)
          d-val (* kd (- last-input value))
          i-term (clamp (+ i-term (* ki error)) -1.0 1.0)
          i-val i-term
          pid (scale (clamp (+ p-val i-val d-val) -1.0 1.0)
                     -1.0 1.0 out-min out-max)]
      [(assoc c
              :i-term i-term
              :last-input value
              :last-time-ms time-ms
              :last-value pid)
       pid])
    [c (:last-value c)]))
