(ns deskmap.core
  (:require [deskmap.config :as cfg]
            [deskmap.util :as util]
            [deskmap.state :as state]
            [deskmap.renderer :as rdr]
            [deskmap.xorg-backend :as raw])
  (:gen-class))

(defn -main [& args]
  (cfg/load-config)
  (state/full-update-state)
  (rdr/render))
