(ns pulina.pages.index-page
  (:require [hiccup.core :as h]
            [hiccup.page :as p]
            [ring.util.response :as r]))

(defn page-content []
  (p/html5
    [:html
     [:head
      [:title "Pulina Chat"]
      [:link {:href "css/main.css" :rel "stylesheet" :type "text/css"}]]
     [:body
      [:div#app]
      (p/include-js "js/main.js")]]))


(defn page [req]
  (-> (page-content)
      h/html
      r/response
      (r/content-type "text/html; charset=utf-8")))