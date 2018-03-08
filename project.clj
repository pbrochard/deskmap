(defproject deskmap "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main ^:skip-aot deskmap.core
  :dependencies [[org.clojure/clojure "1.9.0"]
                 ;;[org.clojars.kingfranz/seesaw "2.0.1"]
                 [seesaw "1.4.5"]]
  :plugins [[cider/cider-nrepl "0.17.0-SNAPSHOT"]]
  :profiles {:uberjar {:aot :all}})
