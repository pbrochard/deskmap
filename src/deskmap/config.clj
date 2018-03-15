(ns deskmap.config
  (:require [clojure.string :as string]
            [seesaw.font :as font]))

(def server-port 10001)

(def config-file (str (System/getProperty "user.home") "/.deskmap.clj"))

(def window-color "#000033")
(def current-window-color "#CC1111")
(def tag-color "#7777ee")


(def window-font (font/font "ARIAL-PLAIN-12"))
(def tag-font (font/font "ARIAL-ITALIC-10"))

(def desk-names {-1 "Fixed", 0 "Main", 1 "Test"})

(defn hsplit [w]
  (if (< (:x w) 512) "<-Left" "Right->"))

(defn vsplit [w]
  (if (< (:y w) 300) "^-Up" "Down-v"))

(defn hidder [w]
  (= (:desk w) -1))

(defn sorter [w]
  [(:desk w) (:x w) (:y w) (- Integer/MAX_VALUE (:focus-time w 0))])

(defn grouper [w]
  [(:desk w)
   (hsplit w)
   (vsplit w)])


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
