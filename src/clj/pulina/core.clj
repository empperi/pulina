(ns pulina.core
  (:require [mount.core :as m]
            [pulina.server :refer [server]])
  (:gen-class))

(defn -main [& args]
  (m/start))