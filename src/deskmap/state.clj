(ns deskmap.state
  (:require [deskmap.xorg-backend :as raw]
            [clojure.set :as set]))

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
  (swap! state untag #(:default %) :default)
  (swap! state update-state))

(defn get-current [st]
  (first (set/select :current st)))

(defn get-default [st]
  (first (set/select :default st)))


(defn focus-by-id [id]
  (raw/focus-by-id id))

(defn focus-default []
  (focus-by-id (:id (get-current @state))))

(defn move-to-desk [id desk]
  (println "moving" id "to" desk)
  (raw/move-to-desk id desk))
