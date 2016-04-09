(ns pulina.view-components
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [dispatch
                                   dispatch-sync
                                   subscribe]]))

(def ENTER_KEY 13)

(defn- on-enter [f & args]
  (fn [e]
    (let [code (-> e .-charCode)]
      (if (= ENTER_KEY code)
        (do
          (.preventDefault e)
          (apply f args))
        e))))

(defn- on-change-update [ratom]
  (fn [e]
    (.preventDefault e)
    (reset! ratom (-> e .-target .-value))))

(defn new-chat
  []
  (let [toggled?  (reagent/atom false)
        input-val (reagent/atom "")
        join-chan (fn []
                    (dispatch [:join-channel @input-val])
                    (reset! input-val "")
                    (reset! toggled? false))]
    (fn new-channel-render []
      [:div.new-chat
       (if @toggled?
         [:div
          [:input
           {:type "text"
            :placeholder "Channel name..."
            :value @input-val
            :on-change (on-change-update input-val)
            :on-key-press (on-enter join-chan)}]
          [:button
           {:on-click #(join-chan)}
           "Join"]]
         [:button
          {:on-click #(reset! toggled? true)}
          "New Chat"])])))

(defn current-user
  []
  (let [current-user (subscribe [:current-user])]
    (fn current-user-render []
      [:div#current-user
       [:span (:name @current-user)]])))

(defn header
  []
  (let [x 1]
    (fn header-render []
      [:header
       [new-chat]
       [current-user]])))

(defn chat-channels
  []
  (let [channels (subscribe [:channels])]
    (fn chat-channels-render []
      [:div.channels
       (if (empty? @channels)
         [:span "No channels, yet!"]
         [:ul
          (doall
            (for [c @channels]
              [:li {:key (str "chan-" (:name c))}
               [:button.channel-btn {:on-click #(dispatch [:channel-selected c])} (:name c)]]))])])))

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
        {:type         "text"
         :value        @msg-input-val
         :on-change    (on-change-update msg-input-val)
         :on-key-press (on-enter send-msg)}]
       [:button
        {:on-click send-msg}
        "Send"]])))