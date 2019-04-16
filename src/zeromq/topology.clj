(ns zeromq.topology
  (:refer-clojure :exclude [send])
  (:require [zeromq.zmq :as z]
            [clojure.pprint :refer [pprint]]
            [zeromq.helper :as h])
  (:import (org.zeromq ZMQ)))

(do
  (defn not-interrupted []
    (not (.. Thread currentThread isInterrupted)))

  (def c (z/context))
  (def s
    (let [so (z/socket c :req)]
      (z/connect so "tcp://localhost:5555")
      so))

  (defn rec-send [socket]
    (let [msg (z/receive-str socket)]
      (println "Msg" msg)
      (z/send-str socket msg)))

  (defn server []
    (let [c (z/context)
          s (z/socket c :rep)]
      (z/bind s "tcp://*:5555")
      (while (not-interrupted)
        (println (rec-send s)))))

  (future (server))

  (do z/charset)

  (defn router []
    (let [c (z/context)
          s (z/socket c :router)
          p (z/poller c)]
      (z/bind s "tcp://*:6666")
      (z/register p s :pollin)
      (while (not-interrupted)
        (z/poll p)
        (println "Receiving message")
        (let [msg (z/receive-all s)
              [id _ content] msg]
          (println "id" id (h/->hex id))
          (println "content" content (z/frame->string content))
          (z/send-all s msg)))))

  (future (router))

  (def dealer
    (let [s (z/socket c :dealer)]
      (z/connect s "tcp://localhost:6666")
      s))

  (defn dealer-send [])

  (defn req-rep [socket msg]
    (z/send-str socket msg)
    (z/receive-str socket))

  (time
    (do (for [_ (range 10)] (req-rep s "awesome"))))

  (def sr
    (let [so (z/socket c :req)]
      (z/connect so "tcp://localhost:6666"))))


(defn send-dealer [msg]
  (z/send-all dealer
    [(.getBytes "" ZMQ/CHARSET)
     (.getBytes msg ZMQ/CHARSET)]))

(defn omg [msg]
  (send-dealer msg)
  (let [[a b] (z/receive-all dealer)]
    (println (String. a ZMQ/CHARSET))
    (println (String. b ZMQ/CHARSET))))

(omg "Alex")

(z/send-str sr "awesome")
(z/receive-str sr)




(-> (->Graph nil nil)
    (add {:tag :server :bind "tcp://*:5555"})
    (add {:tag :client :connect "tcp://localhost:5555"})
    (link :client :req :rep :server)
    (link :server :rep :req :client)
    (begin))

(-> (->Node :rep)
    (tag :awesome)
    (init c)
    (handle handler))


(defprotocol ITaggable
  (tag [this name]))

(defprotocol INode
  (ingest [state node value]))

(defprotocol ISocket
  (init [this context])
  (send [this & messages])
  (receive [this])
  (handle [this fn]))

(defprotocol IBinder
  (bind [this endpoint]))

(defprotocol IConnect
  (connect [this endpoint]))

(defrecord Node [type]
  ITaggable
  (tag [this name]
    (assoc this :tag name))
  ISocket
  (init [this context]
    (let [socket (z/socket context type)]
      (-> this
        (assoc :context context)
        (assoc :socket socket))))
  (send [{:keys [socket]} & messages]
    (z/send-all socket messages))
  (handle [this fn]
    (assoc this :handle fn))
  IConnect
  (connect [{:keys [context]} endpoint]
    (z/connect (z/socket context type) endpoint))
  IBinder
  (bind [{:keys [context]} endpoint]
    (z/bind (z/socket context type) endpoint)))

(defn handler [socket])

(defprotocol IGraph
  (add [this node])
  (link [this a from to b])
  (infer [this])
  (begin [this]))

(defn traverse [graph])


(defrecord Graph [nodes links]
  IGraph
  (add [this node]
    (-> this
      (assoc-in [:nodes (:tag node)] node)))
  (link [this a from to b]
    (-> this
      (update-in [:links a from] (fnil conj []) #{to b})))
  (infer [this]
    this)
  (begin [this]
    (let [context (z/context)]
      this)))

