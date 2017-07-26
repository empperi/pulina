(ns pulina.subscriptions
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   path
                                   reg-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]))

(defn name-for? [n] (fn [c] (= n (:name c))))

(defn- active-channel [db]
  (let [active (:active-channel db)]
    (first (filter (name-for? active) (:channels db)))))

(reg-sub
  :channels
  (fn
    [db _]
    (:channels db)))

(reg-sub
  :active-channel
  (fn
    [db _]
    (active-channel db)))

(reg-sub
  :messages
  (fn
    [db _]
    (-> db active-channel :messages)))

(reg-sub
  :users
  (fn
    [db x]
    (:users db)))

(reg-sub
  :current-user
  (fn
    [db _]
    (:current-user db)))