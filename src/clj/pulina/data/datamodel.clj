(ns pulina.data.datamodel
  (:require [mount.core :as m]
            [taoensso.sente :as sente]
            [com.rpl.specter :as sp]
            [taoensso.sente.server-adapters.http-kit :refer [sente-web-server-adapter]]
            [clojure.core.async :as async :refer [<! <!! >! >!! put! chan go go-loop]]
            [puu.model :as puu]))

(def mgr
  (puu/manager
    "chat-mgr"
    (puu/model
      {:channels [{:name "Channel 1" :messages ["Lol" "noob"]}
                  {:name "Channel 2" :messages ["Trololo"]}
                  {:name "Channel 3" :messages ["Whatta hell bro?" "Muchos gracias"]}]})))

(def changes (puu/subscribe mgr (chan)))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv)
  (def chsk-send!                    send-fn)
  (def connected-uids                connected-uids))

(defn route-handlers []
  {:get-ws-handler ring-ajax-get-or-ws-handshake
   :post-handler ring-ajax-post})

(defn start-broadcaster! []
  (go-loop [i 0]
    (let [d (<!! changes)
          v (puu/version d)]
      (doseq [uid (:any @connected-uids)]
        (chsk-send! uid [:model/changes (puu/version-changes mgr (dec v) v)])))
    (recur (inc i))))

(defmulti dispatch! :id)

(defmethod dispatch! :model/all
  [{uid :uid}]
  (chsk-send! uid [:model/all (puu/model->map (puu/get-version mgr :latest))]))

(defmethod dispatch! :event/new-msg
  [{[chan-name msg] :?data}]
  (puu/do-tx
    mgr
    (fn [x]
      (sp/transform
        [:channels sp/ALL #(= chan-name (:name %)) :messages]
        #(conj % msg)
        x))))

(defmethod dispatch! :default
  [_]
  (comment "ignore"))

(defn start-receiver! []
  (go-loop [i 0]
    (dispatch! (<!! ch-chsk))
    (recur (inc i))))

(m/defstate broadcaster :start (start-broadcaster!)
                        :stop  (async/close! broadcaster))

(m/defstate receiver :start (start-receiver!)
                     :stop  (async/close! receiver))