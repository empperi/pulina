(ns pulina.subscriptions
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]))

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
