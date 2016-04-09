(ns pulina.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [clojure.string :as st]
            [com.rpl.specter :as sp]
            [pulina.data.data-syncer :as syncer]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [taoensso.timbre :as tre]))

(def server-data-keys #{:channels})

(def initial-state
  {:active-channel nil
   :channels       []})

(register-handler
  :initialize
  (fn
    [db _]
    (merge db initial-state)))

(register-handler
  :channel-selected
  (fn
    [db [_ channel]]
    (tre/debug "Changing channel:" (:name channel))
    (assoc-in db [:active-channel] (:name channel))))

(register-handler
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

(register-handler
  :sending-message
  (fn
    [db [_ chan-name msg]]
    (syncer/send-msg! chan-name msg)
    db))

(register-handler
  :new-message
  (fn
    [db [_ {:keys [name]} msg]]
    (when (> (count (st/trim msg)) 0)
      (do
        (dispatch [:sending-message name msg])
        #_(sp/transform
          [:channels sp/ALL #(= name (:name %)) :messages]
          #(conj % msg)
          db)))
    db))