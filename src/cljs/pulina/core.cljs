(ns pulina.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [mount.core :as m]
            [re-frame.core :refer [dispatch-sync subscribe]]
            [pulina.view-components :as view]
            [pulina.login :as login]
            [pulina.subscriptions]
            [pulina.events]
            [pulina.data.data-syncer]))

(enable-console-print!)

;; -- View Components ---------------------------------------------------------

(defn pulina-app-view
  []
  (let [current-user (subscribe [:current-user])]
    (fn pulina-app-view-render
      []
      (if (some? @current-user)
        [:div#content
         [view/header]
         [:div#view
          [view/chat-channels]
          [:div#messages
           [view/messages-list]
           [view/message-input]]]]
        [login/login]))))


;; -- Entry Point -------------------------------------------------------------

(defn render!
  []
  (reagent/render [pulina-app-view] (.getElementById js/document "app")))

(defn ^:export start!
  []
  (dispatch-sync [:initialize])
  (m/start)
  (render!))