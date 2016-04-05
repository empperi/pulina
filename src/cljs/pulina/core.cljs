(ns pulina.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [dispatch-sync]]
            [pulina.view-components :as view]
            [pulina.subscriptions]
            [pulina.events]))

(enable-console-print!)

;; -- View Components ---------------------------------------------------------

(defn pulina-app-view
  []
  [:div#view
   [view/chat-channels]
   [:div#messages
    [view/messages-list]
    [view/message-input]]])


;; -- Entry Point -------------------------------------------------------------

(defn render!
  []
  (reagent/render [pulina-app-view] (.getElementById js/document "app")))

(defn ^:export start!
  []
  (dispatch-sync [:initialize])
  (render!))