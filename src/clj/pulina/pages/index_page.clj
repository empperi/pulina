(ns pulina.pages.index-page
  (:require [hiccup.core :as h]
            [hiccup.page :as p]
            [garden.core :as g]
            [ring.util.response :as r]))

(def styles
  (g/css
    [:body {:font-family "Helvetica"}]))

(defn page-content []
  (p/html5
    [:html
     [:head
      [:title "Pulina Chat"]
      [:style styles]]
     [:body
      [:p "It makes pulina!"]
      (p/include-js "js/main.js")]]))


(defn page [req]
  (-> (page-content)
      h/html
      r/response
      (r/content-type "text/html; charset=utf-8")))