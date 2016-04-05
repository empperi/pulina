(ns pulina.view-components
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [dispatch
                                   dispatch-sync
                                   subscribe]]))

(def ENTER_KEY 13)

(defn chat-channels
  []
  (let [channels (subscribe [:channels])]
    (fn chat-channels-render []
      [:ul.channels
       (doall
         (for [c @channels]
           [:li {:key (str "chan-" (:name c))}
            [:button.channel-btn {:on-click #(dispatch [:channel-selected c])} (:name c)]]))])))

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

(defn message-input
  []
  (let [active-chan (subscribe [:active-channel])
        msg-input-val (reagent/atom "")
        send-msg    (fn [& _]
                      (dispatch [:new-message @active-chan @msg-input-val])
                      (reset! msg-input-val ""))]
    (fn message-input-render
      []
      [:div#msg-input
       [:span.chan-name (:name @active-chan)]
       [:input#msg-input-field
        {:type "text"
         :value @msg-input-val
         :on-change #(reset! msg-input-val (-> % .-target .-value))
         :on-key-press (fn [e]
                         (let [code (-> e .-charCode)]
                           (if (= ENTER_KEY code)
                             (send-msg)
                             e)))}]
       [:button
        {:on-click send-msg}
        "Send"]])))