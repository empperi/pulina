(ns pulina.data.datamodel
  (:require [clojure.core.async :refer [chan]]
            [puu.model :as puu]))

(def mgr (puu/manager "chat-mgr" (puu/model {})))
(def changes (puu/subscribe mgr (chan)))
(def password-db (atom {}))

(defn add-user-pwd! [username pwd]
  (swap! password-db assoc username pwd))

(defn check-login [username pwd]
  (= pwd (get @password-db username)))

(defn uid->user [uid]
  (first
    (filter
      #(= (:username %) uid)
      (:users @mgr))))