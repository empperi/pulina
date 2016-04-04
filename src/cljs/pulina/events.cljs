(ns pulina.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]))

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