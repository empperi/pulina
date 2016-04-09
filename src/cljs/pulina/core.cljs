(ns pulina.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [mount.core :as m]
            [re-frame.core :refer [dispatch-sync]]
            [pulina.view-components :as view]
            [pulina.subscriptions]
            [pulina.events]
            [pulina.data.data-syncer]))

(enable-console-print!)

;; -- View Components ---------------------------------------------------------

(defn pulina-app-view
  []
  [:div#content
   [view/header]
   [:div#view
    [view/chat-channels]
    [:div#messages
     [view/messages-list]
     [view/message-input]]]])


;; -- Entry Point -------------------------------------------------------------

(defn render!
  []
  (reagent/render [pulina-app-view] (.getElementById js/document "app")))

(defn ^:export start!
  []
  (dispatch-sync [:initialize])
  (m/start)
  (render!))