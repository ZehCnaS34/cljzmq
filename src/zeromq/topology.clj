(ns zeromq.topology
  (:refer-clojure :exclude [send])
  (:require [zeromq.zmq :as z]
            [clojure.core.async :as async]
            [clojure.pprint :refer [pprint]]
            [zeromq.helper :as h])
  (:import (org.zeromq ZMQ)))

(defn not-interrupted []
  (not (.. Thread currentThread isInterrupted)))

(defn rec-send [socket]
  (println "I'm here")
  (let [msg (z/receive-str socket)]
    (println "Msg" msg)
    (z/send-str socket msg)))

(defn server [context]
  (println "Starting server on port:5555")
  (let [s (z/socket context :rep)]
    (z/bind s "tcp://*:5555")
    (while (not-interrupted)
      (println (rec-send s)))))

(defn router [c]
  (println "Starting router on port:6666")
  (let [s (z/socket c :router)
        p (z/poller c)]
    (z/bind s "tcp://*:6666")
    (z/register p s :pollin)
    (while (not-interrupted)
      (z/poll p)
      (println "Receiving message")
      (let [msg (z/receive-all s)]
        (pprint (zipmap (range) (map z/frame->string msg)))
        (z/send-all s msg)))))

;(def dealer
;  (let [s (z/socket c :dealer)]
;    (z/connect s "tcp://localhost:6666")
;    s))

(defn req-rep [socket msg]
  (z/send-str socket msg)
  (z/receive-str socket))


;(defn send-dealer [msg]
;  (z/send-all dealer
;    [(.getBytes "" ZMQ/CHARSET)
;     (.getBytes "email" ZMQ/CHARSET)
;     (.getBytes "" ZMQ/CHARSET)
;     (.getBytes "Awesome" ZMQ/CHARSET)
;     (.getBytes "" ZMQ/CHARSET)
;     (.getBytes msg ZMQ/CHARSET)]))
;
;(defn omg [msg]
;  (send-dealer msg)
;  (let [msg (z/receive-all dealer)]
;    (println "Response:")
;    (pprint (zipmap (range) (map #(z/frame->string %) msg)))))

;(time
;  (omg "Alex"))
;
;(time
;  (do
;    (z/send-str sr "awesome")
;    (z/receive-str sr)))

;(def ch  (async/chan))
;
;(async/go
;  (async/>! ch "Hello"))
;
;(async/go
;  (println (async/<! ch)))



;(-> (->Graph nil nil)
;    (add {:tag :server :bind "tcp://*:5555"})
;    (add {:tag :client :connect "tcp://localhost:5555"})
;    (link :client :req :rep :server)
;    (link :server :rep :req :client)
;    (begin))
;
;(-> (->Node :rep)
;    (tag :awesome)
;    (init c)
;    (handle handler))
;
;
;(defprotocol ITaggable
;  (tag [this name]))
;
;(defprotocol INode
;  (ingest [state node value]))
;
;(defprotocol ISocket
;  (init [this context])
;  (send [this & messages])
;  (receive [this])
;  (handle [this fn]))
;
;(defprotocol IBinder
;  (bind [this endpoint]))
;
;(defprotocol IConnect
;  (connect [this endpoint]))
;
;(defrecord Node [type]
;  ITaggable
;  (tag [this name]
;    (assoc this :tag name))
;  ISocket
;  (init [this context]
;    (let [socket (z/socket context type)]
;      (-> this
;        (assoc :context context)
;        (assoc :socket socket))))
;  (send [{:keys [socket]} & messages]
;    (z/send-all socket messages))
;  (handle [this fn]
;    (assoc this :handle fn))
;  IConnect
;  (connect [{:keys [context]} endpoint]
;    (z/connect (z/socket context type) endpoint))
;  IBinder
;  (bind [{:keys [context]} endpoint]
;    (z/bind (z/socket context type) endpoint)))
;
;(defn handler [socket])
;
;(defprotocol IGraph
;  (add [this node])
;  (link [this a from to b])
;  (infer [this])
;  (begin [this]))
;
;(defn traverse [graph])
;
;
;(defrecord Graph [nodes links]
;  IGraph
;  (add [this node]
;    (-> this
;      (assoc-in [:nodes (:tag node)] node)))
;  (link [this a from to b]
;    (-> this
;      (update-in [:links a from] (fnil conj []) #{to b})))
;  (infer [this]
;    this)
;  (begin [this]
;    (let [context (z/context)]
;      this)))

