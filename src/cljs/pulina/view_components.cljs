(ns pulina.view-components
  (:require [reagent.core :as reagent]
            [pulina.form-components :as fc]
            [cljs-time.core :as t]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as tc]
            [re-frame.core :refer [dispatch
                                   dispatch-sync
                                   subscribe]])
  (:import [goog.fx.dom Scroll]))

(def time-of-day-formatter (tf/formatter "HH:mm:ss"))
(def day-of-month-formatter (tf/formatter "dd MMM yyyy"))

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
            :on-change (fc/on-change-update input-val)
            :on-key-press (fc/on-enter join-chan)}]
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

(defn usernname->nickname [users username]
  (:name (first (filter #(= username (:username %)) users))))

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

(defn scroll! [el start end time]
  (.play (Scroll. el (clj->js start) (clj->js end) time)))

(defn scrolled-to-end? [el tolerance]
  ;; at-end?: element.scrollHeight - element.scrollTop === element.clientHeight
  (> tolerance (- (.-scrollHeight el) (.-scrollTop el) (.-clientHeight el))))

(defn messages-list
  []
  (let [active-chan   (subscribe [:active-channel])
        users         (subscribe [:users])
        current-user  (subscribe [:current-user])
        msgs          (subscribe [:messages])
        should-scroll (reagent/atom true)]
    (reagent/create-class
      {:display-name "messages-list"
       :component-did-mount (fn [this]
                              (let [n (reagent/dom-node this)]
                                (scroll! n [0 (.-scrollTop n)] [0 (.-scrollHeight n)] 0)))
       :component-will-update (fn [this]
                                (let [n (reagent/dom-node this)]
                                  (reset! should-scroll (scrolled-to-end? n 100))))
       :component-did-update (fn [this]
                               (let [n       (reagent/dom-node this)]
                                 (when @should-scroll
                                   (scroll! n [0 (.-scrollTop n)] [0 (.-scrollHeight n)] 600))))
       :render (fn []
                 [:ul.messages
                  (doall
                    (map-indexed
                      (fn [idx [[timestamp user] msg]]
                        [:li
                         {:key   (str (:name @active-chan) "-" idx)
                          :class (if (= (:username @current-user) user) "own-msg" "")}
                         [:span.msg-info
                          [:span.time (tf/unparse time-of-day-formatter (t/to-default-time-zone (tc/from-long timestamp)))]
                          [:span.user (usernname->nickname @users user)]]
                         [:span.msg msg]])
                      @msgs))])})))

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
         :on-change    (fc/on-change-update msg-input-val)
         :on-key-press (fc/on-enter send-msg)}]
       [:button
        {:on-click send-msg}
        "Send"]])))