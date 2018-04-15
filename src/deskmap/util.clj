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


(defn ceil [num den]
  (let [q (quot num den)
        r (rem  num den)]
    (if (= 0 r) q (+ 1 q))))

(defn nullify-from [a i]
  "Nullify all elemtns of array a starting at index i"
  (let [l (count a)]
    (into (subvec a 0 (min i l)) (repeat (- l i) 0))))
