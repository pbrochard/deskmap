(ns deskmap.renderer
  (:use [seesaw core border behave graphics keymap])
  (:require [deskmap.config :as cfg]))


(defn original [c g]
;;  (draw g (rounded-rect 3 3 (- (width c) 6) (- (height c) 6) 3)
;;        (style :foreground "#FFFFaa"
;;               :background "#aaFFFF"
  ;;               :stroke 1)))
  )

(defn selected [c g]
  (draw g (rect 2 2 (- (width c) 4) (- (height c) 4))
        (style :foreground :blue
               :background "#FFFFaa"
               :stroke 1)))

(defn make-label
  [id x y text]
  (doto (label :id id
               :border   5
               :text     text
               :location [x y]
               ;;:bounds [x y 255 25]
               :h-text-position :right
               :v-text-position :center
               :font cfg/label-font
               :paint {:before original})
    (config! :bounds :preferred)
    (listen :mouse-entered #(config! % :foreground :blue :paint {:before selected}))
    (listen :mouse-exited #(config! % :foreground :black :paint {:before original}))
    (listen :mouse-clicked (fn [e] (alert "I'm an alert")))))

(defn produce-panel [dm]
  (second 
   (reduce (fn [[i m] x]
             (reduce (fn [[i n] y]
                       [(inc i) (conj n (make-label (keyword (str "lbl" i))
                                                    cfg/hspace-win (* i cfg/vspace)
                                                    (cfg/window-label y)))])
                     [(inc i) (conj m (make-label (keyword (str "lbl" i))
                                                  cfg/hspace-flag (* i cfg/vspace)
                                                  (str (cfg/translate-tags (first x)))))]
                     (second x)))
           [0 []]
           (cfg/sort-and-group dm))))

(defn make-panel [dm]
  (xyz-panel
    :id :xyz
    :items (produce-panel dm)))

(defn render-deskmap [dm]
  (show!
   (let [frm (doto (frame
                    :title   "Desktop Map"
                    :content (border-panel
                              :vgap 5
                              :center (make-panel dm))
                    :size    [600 :by 600])
               (.setFocusTraversalKeysEnabled false))]
     ;;(listen :key-pressed (fn [e] (alert "plop" e))))))
     (map-key frm "K" (fn [e] (config! (select frm [:#lb1]) :foreground :blue :paint {:before selected})) :scope :global)
     (map-key frm "shift K" (fn [e] (config! (select frm [:#lb1]) :foreground :blue :paint {:before original})) :scope :global)
     (map-key frm "UP" (fn [e] (alert "plop" e)) :scope :global)
     (map-key frm "control ENTER" (fn [e] (alert "plop" e)) :scope :global)
     (map-key frm "SPACE" (fn [e] (alert "plop" e)) :scope :global)
     (map-key frm "TAB" (fn [e] (alert "plop" e)) :scope :global)
     frm)))
