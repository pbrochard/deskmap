(ns deskmap.renderer
  (:use [seesaw core border behave graphics keymap color]
        [deskmap.util :only [ceil]])
  (:require [deskmap.config :as cfg]
            [deskmap.state :as state]
            [deskmap.util :as util]))

(def frm (atom nil))

(defn complete-state [dm]
  (into []
        (map (fn [[bd st]]
               (map (fn [w i]
                      [(conj bd i) w])
                    st (iterate inc 0))))
        dm))

(defn translate-state [dm]
  (reduce (fn [acc x]
            (reduce (fn [acc [bd st]]
                      (conj acc [bd (select-keys st [:label :current :default])]))
                    acc
                    x))
          []
          dm))

(defn compute-max [dm]
  (let [len (count (first (first dm)))
        init (vec (take len (iterate identity [])))]
    (reduce (fn [acc [bd _]]
              (loop [acc acc i 0]
                (if (< i len)
                  (recur (if (some #{(bd i)} (acc i))
                           acc
                           (update-in acc [i] conj (bd i)))
                         (inc i))
                  acc)))
            init dm)))

(defn compute-count-max [mx]
  (map count mx))

(defn compute-dividors [mx dv]
  (let [len (count mx)
        compute-split (fn [n ncol] {:div-x ncol :div-y (ceil n ncol)})]
    (loop [acc (vec mx)
           i 0]
      (if (< i len)
        (recur (update-in acc [i] compute-split (dv i)) (inc i))
        acc))))

(defn compute-sizes [dvs lx ly]
  (let [len (count dvs)]
    (loop [acc (vec (take len (iterate identity {})))
           lx lx
           ly ly
           i 0]
      (if (< i len)
        (let [nx (/ lx (:div-x (dvs i)))
              ny (/ ly (:div-y (dvs i)))]
          (recur (update-in acc [i] assoc :dx nx :dy ny)
                 nx ny (inc i)))
        acc))))

(defn compute-pos [bd mx dv]
  (let [len (count bd)]
    (loop [acc (vec (take len (iterate identity {})))
           i 0]
      (if (< i len)
        (let [cnt (count (mx i))
              idx (.indexOf (mx i) (bd i))]
          (recur (update-in acc [i] assoc :px (mod idx (dv i)) :py (int (/ idx (dv i)))) (inc i)))
        acc))))

(defn compute-real-pos [p s]
  (reduce (fn [acc r]
            (assoc acc :x (+ (:x acc) (:x r)) :y (+ (:y acc) (:y r))))
          {:x 0 :y 0}
          (map (fn [x y]
                 {:x (* (:px x) (:dx y))
                  :y (* (:py x) (:dy y))})
               p s)))

(defn translate-pos [dm dv lx ly]
  (let [mx (compute-max dm)
        dvs (compute-dividors (compute-count-max mx) dv)
        sizes (compute-sizes dvs lx ly)]
    {:maxs mx
     :sizes sizes
     :dvs dvs
     :pos (map (fn [[bd w]]
                 (merge (compute-real-pos (compute-pos bd mx dv) sizes) {:w w}))
               dm)}))

(defn to-label [{:keys [x y w]} dx dy]
  (let [current? (:current w)
        default? (:default w)]
    (label :text (:label w)
           :foreground (if current?  cfg/current-fg-color (if default? cfg/default-fg-color cfg/fg-color))
           :background (if current?  cfg/current-bg-color (if default? cfg/default-bg-color (color 0 0 0 0)))
           :bounds [(+ x 1) (+ y 1) (- dx 2) (- dy 2)])))


(defn draw-grid [g dvs sizes px py lvl]
  (let [{nx :div-x ny :div-y} (first dvs)
        {dx :dx dy :dy} (first sizes)]
    (dotimes [x nx]
      (dotimes [y ny]
        (let [rx (+ px (* x dx))
              ry (+ py (* y dy))]
          (when-not (empty? (next (next dvs)))
            (draw-grid g (next dvs) (next sizes) rx ry (inc lvl)))
          (draw g (rect rx ry dx dy) (style :foreground (cfg/grid-colors lvl))))))))

(defn draw-default-background [g dv sizes maxs dm]
  (let [[bd _] (first (filter #(:default (second %)) dm))]
    (dotimes [i (dec (count bd))]
      (let [{:keys [x y]} (compute-real-pos (compute-pos (util/nullify-from bd (inc i)) maxs dv) sizes)
            {:keys [dx dy]} (sizes i)]
        (draw g (rect x y dx dy) (style :background cfg/select-default-bg-color))))))

(defn make-panel [panel dm dflt curr]
  (let [w (width panel)
        h (dec (height panel))]
    (let [{p :pos maxs :maxs dvs :dvs sizes :sizes} (translate-pos dm (cfg/dividors) w h)
          {dx :dx dy :dy} (last sizes)]
      {:paint (fn [c g]
                (draw-default-background g (cfg/dividors) sizes maxs dm)
                (draw-grid g dvs sizes 0 0 0))
       :items (map (fn [w] (to-label w dx dy)) p)})))

(defn redraw-panel [panel dm dflt curr]
  ;;(println panel)
  ;;(println dm)
  (let [{:keys [paint items]} (make-panel panel dm dflt curr)]
    (config! panel :items items :paint paint)))

(defn make-frame []
  (doto (frame
         ;;:on-close :exit
         :title   "Desktop Map"
         :content (xyz-panel
                   :id :main-panel
                   :background cfg/bg-color
                   :preferred-size [cfg/width :by cfg/height]))
    (pack!)
    (.setFocusTraversalKeysEnabled false)
    (.setLocationRelativeTo nil)))

(defn render []
  (reset! frm (make-frame))
  (let [panel (select @frm [:#main-panel])
        redraw-fn (fn [& _]
                    (let [st @state/state
                          dflt (state/get-default st)
                          curr (state/get-current st)]
                      (redraw-panel panel (translate-state (complete-state (cfg/sort-and-group st))) dflt curr)))]
    (redraw-fn)
    (listen panel :component-resized redraw-fn))
  (show! @frm))

(defn show-frame []
  (repaint! @frm)
  (show! @frm))

(defn update-and-render []
  (state/full-update-state)
  (render)
  nil)

(defn open-or-show []
  (if (.isVisible @frm)
    (show-frame)
    (update-and-render)))
