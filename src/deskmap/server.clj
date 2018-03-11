(ns deskmap.server
  (:require [clojure.java.io :as io])
  (:import [java.net ServerSocket]))

(defn read-sock
  "Read a line of textual data from the given socket"
  [socket]
  (.readLine (io/reader socket)))

(defn write-sock
  "Send the given string message out over the given socket"
  [socket msg]
  (let [writer (io/writer socket)]
      (.write writer msg)
      (.flush writer)))

(defn serve [port handler]
  (with-open [server-sock (ServerSocket. port)]
    (loop []
      (with-open [sock (.accept server-sock)]
        (let [msg-in (read-sock sock)
              msg-out (handler msg-in)]
          (write-sock sock msg-out)))
      (recur))))
