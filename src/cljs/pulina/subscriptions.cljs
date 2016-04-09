(ns pulina.subscriptions
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]))

(defn name-for? [n] (fn [c] (= n (:name c))))

(defn- active-channel [db]
  (let [active (reaction (:active-channel @db))]
    (reaction (first (filter (name-for? @active) (:channels @db))))))

(register-sub
  :channels
  (fn
    [db _]
    (reaction (:channels @db))))

(register-sub
  :active-channel
  (fn
    [db _]
    (active-channel db)))

(register-sub
  :messages
  (fn
    [db _]
    (let [active-channel (active-channel db)]
      (reaction (:messages @active-channel)))))

(register-sub
  :users
  (fn
    [db _]
    (reaction (:users @db))))

(register-sub
  :current-user
  (fn
    [db _]
    (let [current-user (reaction (:current-user @db))]
      (reaction (first (filter (name-for? @current-user) (:users @db)))))))