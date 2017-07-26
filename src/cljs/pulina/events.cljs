(ns pulina.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [clojure.string :as st]
            [com.rpl.specter :as sp]
            [pulina.data.data-syncer :as syncer]
            [pulina.transit-xhr :as xhr]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   reg-fx
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [taoensso.timbre :as tre]
            [mount.core :as m]
            [pulina.data.websocket :as ws]))

(def server-data-keys #{:channels :users})

(def initial-state
  {:active-channel nil
   :channels       []})

(reg-fx :init-ws-connection
  (fn []
    (tre/info "Initializing websocket communications")
    (m/start)))

(reg-fx :xhr-send
  (fn
    [{:keys [path data method on-success on-failure]}]
    (xhr/transit-xhr {:method method
                      :url path
                      :data data
                      :on-complete #(dispatch [on-success %])
                      :on-failure  #(dispatch [on-failure %])})))

(reg-fx :ws-send
  (fn
    [event]
    (ws/chsk-send! event)))

(reg-event-db
  :initialize
  (fn
    [db _]
    (merge db initial-state)))

(reg-event-db
  :channel-selected
  (fn
    [db [_ channel]]
    (tre/debug "Changing channel:" (:name channel))
    (assoc-in db [:active-channel] (:name channel))))

(reg-event-db
  :server-data
  (fn
    [db [_ data]]
    (let [init? (empty? (:channels db))
          res (reduce
                (fn [d k] (assoc d k (k data)))
                db
                server-data-keys)]
      (when init?
        (dispatch [:channel-selected (first (:channels res))]))
      res)))

(reg-event-db
  :sending-message
  (fn
    [db [_ chan-name msg]]
    (syncer/send-msg! chan-name msg)
    db))

(reg-event-db
  :new-message
  (fn
    [db [_ {:keys [name]} msg]]
    (when (> (count (st/trim msg)) 0)
      (dispatch [:sending-message name msg]))
    db))

(reg-event-db
  :new-channel
  (fn
    [db [_ chan-name]]
    (syncer/create-channel! chan-name)
    db))

(reg-event-db
  :join-channel
  (fn
    [db [_ chan-name]]
    (tre/info "Joining channel" chan-name)
    (when (nil? (->> db
                     :channels
                     (filter (fn [c] (= chan-name (:name c))))
                     first))
      (dispatch [:new-channel chan-name]))
    (dispatch [:channel-selected {:name chan-name}])
    db))

(reg-event-fx
  :login
  (fn
    [{db :db} [_ user]]
    {:xhr-send {:path "/login"
                :method :post
                :data user
                :on-success :login-succeeded
                :on-failure :login-failed}}))

(reg-event-fx
  :login-succeeded
  (fn
    [{db :db} [_ user]]
    (tre/info "Login succeeded " user)
    {:init-ws-connection []
     :ws-send [:model/all]
     :db      (assoc-in db [:current-user] user)}))

(reg-event-fx
  :create-user
  (fn
    [{db :db} [_ user]]
    {:xhr-send {:path "/user"
                :method :post
                :data user
                :on-success :user-created
                :on-failure :user-creation-failed}}))