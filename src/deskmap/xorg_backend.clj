(ns deskmap.xorg-backend
  (:use [clojure.java.shell :only [sh]])
  (:require [clojure.string :as string]
            [deskmap.util :as util]))


(defn focus-window []
  (util/parse-int (string/replace (:out (sh "xdotool" "getwindowfocus")) #"\n" "")))

(defn win-info [s foc]
  (let [nfo (update-in
             (util/doto-map (zipmap [:id :desk :x :y :w :h :class :host :label]
                                    (string/split (string/replace s #" +" " ") #" " 9))
                            [:desk :x :y :w :h]
                            util/parse-int)
             [:id] util/parse-hex)]
    (if (= (:id nfo) foc)
      (assoc nfo :current true :focus-time (util/now-in-millis))
      nfo)))

(defn raw-state []
  (let [foc (focus-window)]
    (into #{}
          (map #(win-info % foc) (string/split (:out (sh "wmctrl" "-xGi" "-l")) #"\n")))))

(defn focus-by-id [id]
  (println (str "xdotool " "windowactivate " "--sync " id))
  (sh "xdotool" "windowactivate" "--sync" (str id))
  (Thread/sleep 250))


(defn move-to-desk [id desk]
  (sh "xdotool" "set_desktop_for_window" (str id) (str desk))
  (focus-by-id id))
