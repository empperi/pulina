(ns pulina.server
  (:require [mount.core :as m]
            [org.httpkit.server :as httpkit]
            [compojure.core :as c]
            [compojure.route :as r]
            [taoensso.sente.server-adapters.http-kit :refer [sente-web-server-adapter]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [pulina.pages.index-page :as index]
            [pulina.data.comms :as dc]
            [pulina.data.schedulers]))

(let [{:keys [get-ws-handler post-handler]} (dc/route-handlers)]
  (c/defroutes handlers
    (c/GET "/" [] index/page)
    (c/GET  "/chsk" req (get-ws-handler  req))
    (c/POST "/chsk" req (post-handler req))))

(def routes
  (-> (c/routes
        (r/resources "/js" {:root "js"})
        (r/resources "/css" {:root "css"})
        handlers)
      wrap-keyword-params
      wrap-params))

(m/defstate server :start (httpkit/run-server routes {:port 8080})
                   :stop  (server :timeout 500))