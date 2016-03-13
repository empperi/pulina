(ns pulina.core
  (:require [mount.core :as m]
            [pulina.server :refer [server]]))

(defn -main [& args]
  (m/start))