(ns pulina.security
  (:require [taoensso.timbre :as tre]
            [pulina.data.datamodel :as model]
            [puu.model :as puu]))

(defn login [username password]
  (when (model/check-login username password)
    (model/uid->user username)))

(defn create-user [user]
  (tre/info "Creating user" (dissoc user :password))
  (model/add-user-pwd! (:username user) (:password user))
  (puu/do-tx
    model/mgr
    (fn [x]
      (update-in x [:users] (fn [users]
                              (if (nil? users)
                                [(dissoc user :password)]
                                (conj users (dissoc user :password))))))))
