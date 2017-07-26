(ns pulina.data.schedulers
  (:require [clojure.core.async :as async :refer [<! <!! >! >!! put! chan go go-loop]]
            [pulina.data.datamodel :as model]
            [puu.model :as puu]
            [pulina.data.dispatch :as dd]
            [pulina.data.comms :as dc]
            [mount.core :as m]
            [taoensso.timbre :as tre]))

(defn start-broadcaster! []
  (tre/info "Starting model change broadcaster")
  (go-loop [i 0]
    (let [d (<!! model/changes)
          v (puu/version d)]
      (doseq [uid (:any @dc/connected-uids)]
        (dc/chsk-send! uid [:model/changes (puu/version-changes model/mgr (dec v) v)])))
    (recur (inc i))))

(defn start-receiver! []
  (tre/info "Starting channel receiver")
  (go-loop [i 0]
    (dd/dispatch! (<!! dc/ch-chsk))
    (recur (inc i))))

(m/defstate broadcaster :start (start-broadcaster!)
                        :stop (async/close! broadcaster))

(m/defstate receiver :start (start-receiver!)
                     :stop (async/close! receiver))