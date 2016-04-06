(ns pulina.data.datamodel
  (:require [mount.core :as m]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [sente-web-server-adapter]]
            [clojure.core.async :as async :refer [<! <!! >! >!! put! chan go go-loop]]))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
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
           (<! (async/timeout 10000))
           (println (format "Broadcasting server>user: %s" @connected-uids))
           (doseq [uid (:any @connected-uids)]
             (chsk-send! uid
                         [:some/broadcast
                          {:what-is-this "A broadcast pushed from server"
                           :how-often    "Every 10 seconds"
                           :to-whom      uid
                           :i            i}]))
           (recur (inc i))))

(m/defstate broadcaster :start (start-broadcaster!)
                        :stop  (async/close! broadcaster))