(ns pulina.data.dispatch
  (:require [puu.model :as puu]
            [pulina.data.datamodel :as model]
            [com.rpl.specter :as sp]
            [taoensso.timbre :as tre]
            [pulina.data.comms :as dc]))

(def ignored-dispatch-ids #{:chsk/uidport-open :chsk/ws-ping})

(defmulti dispatch! :id)

(defmethod dispatch! :model/all
  [{uid :uid}]
  (dc/chsk-send! uid [:model/all (puu/model->map (puu/get-version model/mgr :latest))]))

(defmethod dispatch! :event/new-msg
  [{[chan-name msg] :?data, uid :uid}]
  (puu/do-tx
    model/mgr
    (fn [x]
      (sp/transform
        [:channels sp/ALL #(= chan-name (:name %)) :messages]
        #(conj % {:msg msg :user uid})
        x))))

(defmethod dispatch! :event/new-chan
  [{[chan-name] :?data}]
  (tre/info "Creating a new Channel:" chan-name)
  (puu/do-tx
    model/mgr
    (fn [x]
      (if (->> x
               :channels
               (filter (fn [c] (= chan-name (:name c))))
               first
               some?)
        x
        (update-in x [:channels] conj {:name chan-name :messages []})))))

(defmethod dispatch! :default
  [{id :id}]
  (when (nil? (ignored-dispatch-ids id))
    (tre/warn "Unknown dispatch value:" id)))