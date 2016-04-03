(ns pulina.core(:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]))

(enable-console-print!)

(def initial-state
  {:channels [{:name "Channel 1" :active? true :messages ["Lol" "noob"]}
              {:name "Channel 2" :active? false :messages ["Trololo"]}
              {:name "Channel 3" :active? false :messages ["Whatta hell bro?" "Muchos gracias"]}]})

;; -- Event Handlers ----------------------------------------------------------


(register-handler                 ;; setup initial state
  :initialize                     ;; usage:  (dispatch [:initialize])
  (fn
    [db _]
    (merge db initial-state)))    ;; what it returns becomes the new state

(register-handler
  :channel-selected
  (fn
    [db [_ channel]]
    (update-in db [:channels] (fn [chans]
                                (for [c chans]
                                  (if (= (:name c) (:name channel))
                                    (assoc-in c [:active?] true)
                                    (assoc-in c [:active?] false)))))))

;; -- Subscription Handlers ---------------------------------------------------

(register-sub
  :channels
  (fn
    [db _]
    (reaction (:channels @db))))

(register-sub
  :active-channel
  (fn
    [db _]
    (let [chans (reaction (:channels @db))]
      (reaction (first (filter (comp true? :active?) @chans))))))

(register-sub
  :messages
  (fn
    [db _]
    (let [chans          (reaction (:channels @db))
          active-channel (reaction (first (filter (comp true? :active?) @chans)))]
      (reaction (:messages @active-channel)))))

;; -- View Components ---------------------------------------------------------

(defn chat-channels
  []
  (let [channels (subscribe [:channels])]
    (fn chat-channels-render []
      [:ul.channels
       (doall
         (for [c @channels]
           [:li {:key (str "chan-" (:name c))}
            [:button {:on-click #(dispatch [:channel-selected c])} (:name c)]]))])))

(defn messages-list
  []
  (let [active-chan (subscribe [:active-channel])
        msgs        (subscribe [:messages])]
    (fn messages-list-render
      []
      [:ul.messages
       (doall
         (map-indexed
           (fn [idx msg] [:li
                          {:key (str (:name @active-chan) "-" idx)}
                          msg])
           @msgs))])))

(defn pulina-app-view
  []
  [:div
   [chat-channels]
   [messages-list]])


;; -- Entry Point -------------------------------------------------------------


(defn ^:export start!
  []
  (dispatch-sync [:initialize])
  (reagent/render [pulina-app-view]
                  (js/document.getElementById "app")))