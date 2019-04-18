(do
  (ns zeromq.system
    (:require [zeromq.zmq :as z]
              [zeromq.topology :as t]))

  (def state {})

  (defn context [])

  (defn socket [])

  (defn system []
      {:sockets {:server {:creator t/server}}})
                 ;:router {:creator t/router}}})


  (defn start-socket [system name]
    (let [context (:context system)
          f (get-in system [:sockets name :creator])]
      (println context)
      (assoc-in system [:sockets name]
        (future (f context)))))

  (defn stop-socket [system name]
    (println (str "Stopping " name "..."))
    (let [f (get-in system [:sockets name])]
      (future-cancel f)))

  (defn start [system]
    (let [context (z/context)]
      (-> system
          (assoc-in [:context] context)
          (start-socket :server)
          (start-socket :router))))

  (defn stop [system]
    (stop-socket system :server)
    (println "Closing context")
    (.term (:context system)))

  (def d (-> (system)
             (start)))

  (def c (:context d))
  (def s (z/socket c :req))
  (z/connect s "tcp://localhost:5555")

  (defn req-rep [socket msg]
    (z/send-str socket msg)
    (println "hoooooo")
    (z/receive-str socket)))

(time
  (doseq [f (range 30)]
    (req-rep s "hi")))

