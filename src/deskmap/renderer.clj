(ns deskmap.renderer
  (:use [seesaw core border behave graphics keymap])
  (:require [deskmap.config :as cfg]
            [deskmap.state :as state]))

(def frm (atom nil))

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
         :title   "Desktop Map"
         :content (make-panel dm)
         :size    [600 :by 800])
    (.setFocusTraversalKeysEnabled false)
    (.setLocationRelativeTo nil)))

(declare redraw-panel)

(defn do-update-state [e]
  (hide! @frm)
  (state/full-update-state)
  (redraw-panel)
  (show! @frm))

(defn do-focus [e]
  (hide! @frm)
  (println (selection (select @frm [:#listbox])))
  (state/focus-by-id (:id (selection (select @frm [:#listbox]))))
  (show! @frm))



(defn add-handler []
  (map-key @frm "control L" do-update-state :scope :global)
  (map-key @frm "shift F" do-focus :scope :global)
  (map-key @frm "SPACE" do-focus :scope :global)
  (map-key @frm "shift SHIFT" do-focus :scope :global)
  ;;(listen :key-pressed (fn [e] (alert "plop" e))))))
  ;;(map-key frm "K" (fn [e] (config! (select frm [:#lb1]) :foreground :blue :paint {:before selected})) :scope :global)
  ;;(map-key frm "shift K" (fn [e] (config! (select frm [:#lb1]) :foreground :blue :paint {:before original})) :scope :global)
  ;;(map-key frm "UP" (fn [e] (alert "plop" e)) :scope :global)
  ;;(map-key frm "control ENTER" (fn [e] (alert "plop" e)) :scope :global)
  ;;(map-key frm "SPACE" (fn [e] (alert "plop" e)) :scope :global)
  ;;(map-key frm "TAB" (fn [e] (alert "plop" e)) :scope :global)

  (listen (select @frm [:#listbox]) :mouse-clicked do-focus))


(defn redraw-panel []
  (let [st @state/state]
    (config! @frm :content (make-panel st))
    (selection! (select @frm [:#listbox]) (first (filter :current st)))
    (add-handler)
    ;;(state/place-window)))
    ))

(defn render []
  (reset! frm (make-frame @state/state))
  (redraw-panel)
  (show! @frm))
