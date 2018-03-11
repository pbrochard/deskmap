(ns deskmap.renderer
  (:use [seesaw core border behave graphics keymap])
  (:require [deskmap.config :as cfg]
            [deskmap.state :as state]))

(def frm (atom nil))

(defn get-listbox []
  (select @frm [:#listbox]))

(defn get-selection []
  (selection (get-listbox)))

(defn produce-panel [dm]
  (reduce (fn [m x]
            (reduce (fn [n y]
                      (conj n y))
                    (conj m (first x))
                    (second x)))
          []
          (cfg/sort-and-group dm)))

(defn render-fn [renderer info]
  (let [v (:value info)]
    (apply config! renderer
           (if (map? v)
             [:text (cfg/window-label v)
              :font cfg/window-font
              :foreground (if (:current v)
                            cfg/current-window-color
                            cfg/window-color)]
             [:text (cfg/tag-label v) :font cfg/tag-font :foreground cfg/tag-color]))))

(defn make-panel [dm]
  (scrollable
   (listbox :id :listbox
            :selection-mode :single
            :model (produce-panel dm)
            :renderer render-fn)))

(defn make-frame [dm]
  (doto (frame
         ;;:on-close :exit
         :title   "Desktop Map"
         :content (make-panel dm)
         :size    [600 :by 800])
    (.setFocusTraversalKeysEnabled false)
    (.setLocationRelativeTo nil)))

(declare redraw-panel)

(defn do-validate [e]
  (hide! @frm)
  (println (get-selection))
  (state/focus-by-id (:id (get-selection))))

(defn do-focus [e]
  (do-validate e)
  (repaint! @frm)
  (show! @frm))

(defn do-update-state [e]
  (hide! @frm)
  (state/full-update-state)
  (redraw-panel)
  (show! @frm))

(defn reload-config [e]
  (cfg/load-config)
  (redraw-panel))

(defn do-close [e]
  (state/focus-default)
  (hide! @frm))

(def bind-keys
  {"ENTER" do-validate
   "ESCAPE" do-close
   "control L" do-update-state
   "shift F" do-focus
   "SPACE" do-focus
   "shift SHIFT" do-focus
   "control R" reload-config})

(defn add-handler []
  (doseq [[k f] bind-keys]
    (map-key @frm k f :scope :global))
  (listen (get-listbox) :mouse-clicked do-focus))


(defn redraw-panel []
  (let [st @state/state]
    (config! @frm :content (make-panel st))
    (selection! (get-listbox) (state/get-current st))
    (add-handler)
    ;;(state/place-window)))
    ))

(defn render []
  (reset! frm (make-frame @state/state))
  (redraw-panel)
  (show! @frm))
