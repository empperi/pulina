(ns pulina.server
  (:require [mount.core :as m]
            [org.httpkit.server :as httpkit]
            [compojure.core :as c]
            [compojure.route :as r]
            [pulina.pages.index-page :as index]))

(defn async-handler [ring-request]
  ;; unified API for WebSocket and HTTP long polling/streaming
  (httpkit/with-channel ring-request channel                ; get the channel
    (if (httpkit/websocket? channel)                        ; if you want to distinguish them
      (httpkit/on-receive channel (fn [data]                ; two way communication
                                    (httpkit/send! channel data)))
      (httpkit/send! channel {:status  200
                              :headers {"Content-Type" "text/plain"}
                              :body    "Long polling?"}))))

(c/defroutes all-routes
  (c/GET "/" [] index/page)
  (c/GET "/ws" [] async-handler))

(def routes
  (c/routes
    (r/resources "/js" {:root "js"})
    (r/resources "/css" {:root "css"})
    all-routes))

(m/defstate server :start (httpkit/run-server routes {:port 8080})
                   :stop  (server :timeout 500))