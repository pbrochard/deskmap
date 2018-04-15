(ns deskmap.config
  (:require [clojure.string :as string]
            [seesaw.font :as font]
            [seesaw.color :as color])
  (:use [deskmap.util :only [ceil]]))

(def width 600)
(def height 800)

(def server-port 10001)

(def config-file (str (System/getProperty "user.home") "/.deskmap.clj"))

(def grid-colors ["blue" "green" "red"])

(def bg-color "#eeeeec")
(def fg-color "black")

(def default-bg-color "#3465a4")
(def default-fg-color "#eeeeec")

(def current-bg-color "#73d216")
(def current-fg-color "black")

(def select-default-bg-color (color/color 255 180 0 50))

(def window-color "#000033")
(def current-window-color "#CC1111")
(def tag-color "#7777ee")


(def window-font (font/font "ARIAL-PLAIN-12"))
(def tag-font (font/font "ARIAL-ITALIC-10"))

(def desk-names {-1 "Fixed", 0 "Main", 1 "Test"})

(defn hsplit [w]
  (if (< (:x w) 960) 0 1))

(defn vsplit [w]
  (if (< (:y w) 540) 0 1))

(defn hidder [w]
  (= (:desk w) -1))

;;(defn sorter [w]
;;  [(:desk w) (:x w) (:y w) (- Integer/MAX_VALUE (:focus-time w 0))])

(defn sorter [w]
  [(:desk w) (- Integer/MAX_VALUE (:focus-time w 0))])


(defn grouper [w]
  [(:desk w)
   (vsplit w)
   (hsplit w)])

(defn dividors []
  [2 1 2 1])


(defn translate-desk [tg]
  (assoc-in tg [0] (if-let [name (get desk-names (tg 0))]
                     (str (inc (tg 0)) ":" name)
                     (inc (tg 0)))))

(defn translate-tags [tg]
  (translate-desk tg))

(defn tag-label [tg]
  (string/join ", "  (translate-desk tg)))

(defn window-label [w]
  (str "  | " (second (string/split (:class w) #"\.")) ": " (:label w)))


(defn sort-and-group [raw]
  (sort-by first
           (group-by grouper
                     (sort-by sorter
                              (remove hidder raw)))))

(defn load-config []
  (try
    (load-file config-file)
    (catch Exception e
      (println "caught: " (.getMessage e)))))
