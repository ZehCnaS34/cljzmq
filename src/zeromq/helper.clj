(ns zeromq.helper
  (:require [zeromq.zmq :as z])
  (:import [org.zeromq ZMQ]
           (clojure.lang ISeq)))

(defmulti to-bytes
  (fn [v] (cond
            (bytes? v) :bytes
            :else (type v))))

(defmethod to-bytes :bytes [v] v)

(defmethod to-bytes java.lang.String
  [v]
  (.getBytes v ZMQ/CHARSET))

(defmethod to-bytes java.lang.Long
  [b]
  (to-bytes (str b)))

(defmethod to-bytes clojure.lang.Keyword
  [v]
  (to-bytes (str v)))

(defmulti from-bytes
  (fn [v] (type v)))


(defn build-frames [& frames]
  (map to-bytes (interleave (repeat "") frames)))


(defn frames-extractor [socket & frames]
  (let [msg (filter (complement empty?) (z/receive-all socket))]
    (zipmap frames msg)))


(defn ->hex [bytes]
  (format "%s" (java.math.BigInteger. bytes)))

