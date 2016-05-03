(ns com.lemondronor.pid)


(defn scale [x in-min in-max out-min out-max]
  (+ (/ (* (- x in-min) (- out-max out-min)) (- in-max in-min)) out-min))


(defn clamp [x min max]
  (cond
    (> x max) max
    (< x min) min
    :default x))


(defn set-tunings
  [state kp ki kd]
  (let [period-s (/ (:period-ms state) 1000.0)]
    (assoc state
           :kp kp
           :ki (* ki period-s)
           :kd (/ kd period-s))))


(defn set-period
  [state period]
  (let [ratio (/ period (:period-ms state))]
    (assoc state
           :ki (* (:ki state) ratio)
           :kd (/ (:kd state) ratio)
           :period-ms period)))


(defn pid
  ([state]
   (-> state
       (assoc :integrator 0 :derivator 0 :last-time-ms 0 :last-value 0)
       (set-tunings (:kp state) (:ki state) (:kd state))))
  ([state time-ms value]
   (if (>= (- time-ms (:last-time-ms state)) (:period-ms state))
     (let [{:keys [set-point kp kd ki integrator derivator bounds]} state
           [in-min in-max out-min out-max] bounds
           value (scale (clamp value in-min in-max) in-min in-max -1.0 1.0)
           sp (scale (clamp set-point in-min in-max) in-min in-max -1.0 1.0)
           error (- sp value)
           p-val (* kp error)
           d-val (* kd (- error derivator))
           integrator (clamp (+ integrator error) -1.0 1.0)
           i-val (* integrator ki)
           pid (scale (clamp (+ p-val i-val d-val) -1.0 1.0)
                      -1.0 1.0 out-min out-max)]
       [(assoc state
               :integrator integrator
               :derivator error
               :last-time-ms time-ms
               :last-value pid)
        pid])
     [state (:last-value state)])))
