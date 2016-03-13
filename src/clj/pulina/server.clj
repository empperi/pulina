(ns pulina.server
  (:require [mount.core :as m]
            [org.httpkit.server :as httpkit]))

(defn async-handler [ring-request]
  ;; unified API for WebSocket and HTTP long polling/streaming
  (httpkit/with-channel ring-request channel                ; get the channel
                        (if (httpkit/websocket? channel)    ; if you want to distinguish them
                          (httpkit/on-receive channel (fn [data] ; two way communication
                                                        (httpkit/send! channel data)))
                          (httpkit/send! channel {:status  200
                                                  :headers {"Content-Type" "text/plain"}
                                                  :body    "Long polling?"}))))

(m/defstate server :start (httpkit/run-server async-handler {:port 8080})
                   :stop  (server))