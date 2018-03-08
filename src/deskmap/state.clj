(ns deskmap.state
  (:require [deskmap.xorg-backend :as raw]))

(def state (atom #{}))

(defn update-state [st]
  (reduce (fn [m x]
            (if-let [old (first (filter #(= (:id %) (:id x)) st))]
              (conj m (merge old x))
              (conj m x)))
          #{}
          (raw/raw-state)))

(defn tag [st pred k v]
  (into #{}
        (map #(if (pred %)
                (assoc-in % [k] v)
                %)
             st)))

(defn untag [st pred & ks]
  (into #{}
        (map #(if (pred %)
                (apply dissoc % ks)
                %)
             st)))


(defn full-update-state []
  (swap! state untag #(:current %) :current)
  (swap! state update-state))


(defn focus-by-id [id]
  (raw/focus-by-id id)
  (full-update-state))
