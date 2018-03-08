(ns deskmap.util)

(defn doto-map [m ks f & args]
  (reduce #(apply update-in %1 [%2] f args) m ks))

(defn parse-int [x]
  (try
    (Integer/parseInt x)
    (catch NumberFormatException e 0)))

(defn parse-hex [x]
  (try
    (Integer/decode x)
    (catch NumberFormatException e 0)))

(defn now-in-millis []
  (System/currentTimeMillis))
