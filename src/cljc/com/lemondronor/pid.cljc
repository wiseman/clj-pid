(ns com.lemondronor.pid)


(defn scale [x in-min in-max out-min out-max]
  (+ (/ (* (- x in-min) (- out-max out-min)) (- in-max in-min)) out-min))


(defn clamp [x min max]
  (cond
    (> x max) max
    (< x min) min
    :default x))


(defn pid
  ([state]
   (assoc state :integrator 0 :derivator 0))
  ([state value]
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
       [(assoc state :integrator integrator :derivator error) pid])))
