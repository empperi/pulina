(ns pulina.server
  (:require [mount.core :as m]
            [org.httpkit.server :as httpkit]
            [compojure.core :as c]
            [compojure.route :as r]
            [cognitect.transit :as tr]
            [taoensso.sente.server-adapters.http-kit :refer [sente-web-server-adapter]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.session :refer [wrap-session]]
            [pulina.pages.index-page :as index]
            [pulina.data.comms :as dc]
            [pulina.data.schedulers]
            [pulina.security :as sec])
  (:import (java.io StringWriter ByteArrayOutputStream)))

(defn ->transit-str [x]
  (with-open [os (ByteArrayOutputStream.)]
    (let [w (tr/writer os :json)]
      (tr/write w x)
      (.toString os))))

(let [{:keys [get-ws-handler post-handler]} (dc/route-handlers)]
  (c/defroutes ws-handlers
    (c/GET  "/chsk" req (get-ws-handler  req))
    (c/POST "/chsk" req (post-handler req))))

(c/defroutes rest-routes
  (c/GET "/" [] index/page)
  (c/POST "/login" req (if-let [user (sec/login
                                       (-> req :params :username)
                                       (-> req :params :password))]
                         {:status 200 :body user :session {:uid (:username user)}}
                         {:status 403 :body "Forbidden"}))
  (c/POST "/user" req (sec/create-user (:params req))))

(def routes
  (-> (c/routes
        (r/resources "/js" {:root "js"})
        (r/resources "/css" {:root "css"})
        ws-handlers
        (wrap-restful-format rest-routes :formats [:transit-json]))
      wrap-session
      wrap-keyword-params
      wrap-params))

(m/defstate server :start (httpkit/run-server routes {:port 8080})
                   :stop  (server :timeout 500))