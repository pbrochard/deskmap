(ns deskmap.config
  (:require [clojure.string :as string]
            [seesaw.font :as font]))

(def window-color "#000033")
(def current-window-color "#CC1111")
(def tag-color "#7777ee")


(def window-font (font/font "ARIAL-PLAIN-12"))
(def tag-font (font/font "ARIAL-ITALIC-10"))

(def desk-names {0 "Main", 1 "Test"})

(defn hsplit [w]
  (if (< (:x w) 512) "<-Left" "Right->"))

(defn vsplit [w]
  (if (< (:y w) 300) "^-Up" "Down-v"))


(defn sorter [w]
  [(:desk w) (:x w) (:y w) (- Integer/MAX_VALUE (:focus-time w 0))])

(defn grouper [w]
  [(:desk w)
   (hsplit w)
   (vsplit w)])


(defn translate-desk [tg]
  (assoc-in tg [0] (if-let [name (get desk-names (tg 0))]
                     (str (tg 0) ":" name)
                     (tg 0))))

(defn translate-tags [tg]
  (translate-desk tg))

(defn tag-label [tg]
  (string/join ", "  (translate-desk tg)))

(defn window-label [w]
  (str "     " (second (string/split (:class w) #"\.")) ": " (:label w)))

;;(defn grouper [w]
;;  [(:desk w)
;;   (cond (< (:y w) 250) 0
;;         (< (:y w) 500) 1
;;         :else 2)
;;   (< (:x w) 1000)])


(defn sort-and-group [raw]
  (sort-by first
           (group-by grouper
                     (sort-by sorter raw))))
