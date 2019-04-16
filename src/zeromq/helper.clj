(ns zeromq.helper)


(defn ->hex [bytes]
  (format "%s" (java.math.BigInteger. bytes)))

