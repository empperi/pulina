(ns pulina.view-components
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [dispatch
                                   dispatch-sync
                                   subscribe]]))

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