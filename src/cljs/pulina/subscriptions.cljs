(ns pulina.subscriptions
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]))

(defn channel-for? [n] (fn [c] (= n (:name c))))

(register-sub
  :channels
  (fn
    [db _]
    (reaction (:channels @db))))

(register-sub
  :active-channel
  (fn
    [db _]
    (let [active (reaction (:active-channel @db))]
      (println active)
      (reaction (first (filter (channel-for? @active) (:channels @db)))))))

(register-sub
  :messages
  (fn
    [db _]
    (let [active-channel-name (reaction (:active-channel @db))
          active-channel      (reaction (first (filter (channel-for? @active-channel-name) (:channels @db))))]
      (reaction (:messages @active-channel)))))
