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
  [:div
   [view/chat-channels]
   [view/messages-list]])


;; -- Entry Point -------------------------------------------------------------


(defn ^:export start!
  []
  (dispatch-sync [:initialize])
  (reagent/render [pulina-app-view]
                  (js/document.getElementById "app")))