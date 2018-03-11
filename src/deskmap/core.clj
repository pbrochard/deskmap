(ns deskmap.core
  (:require [deskmap.config :as cfg]
            [deskmap.util :as util]
            [deskmap.state :as state]
            [deskmap.renderer :as rdr]
            [deskmap.xorg-backend :as raw]
            [deskmap.server :as server])
  (:gen-class))


(defn handle-message [msg]
  (println msg)
  (when (= msg "open")
    (rdr/open-or-show))
  msg)

(defn -main [& args]
  (cfg/load-config)
  (future (server/serve cfg/server-port handle-message))
  (rdr/update-and-render))
