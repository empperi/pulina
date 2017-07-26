(ns pulina.data.data-syncer
  (:require-macros
    [cljs.core.async.macros :as asyncm :refer [go go-loop]])
  (:require
    [mount.core :as m]
    [taoensso.timbre :as tre]
    [re-frame.core :refer [dispatch reg-fx reg-event-fx]]
    [cljs.core.async :as async :refer [<! >! put! chan]]
    [taoensso.sente :as sente :refer [cb-success?]]
    [puu.model :as puu]
    [pulina.data.websocket :as ws]))

(def mgr
  (puu/manager
    "chat-mgr"
    (puu/model {})))

(def changes (puu/subscribe mgr (chan)))

; ---------------------------------------
; communication event dispatching
(defmulti event-dispatch! first)

(defmethod event-dispatch! :model/all
  [[_ data]]
  (tre/debug "Received initial model data: " data)
  (puu/do-tx mgr (fn [_] (:data data))))

(defmethod event-dispatch! :model/changes
  [[_ changes]]
  (tre/debug "Received model changeset:" changes)
  (puu/apply-changeset mgr changes))

(defmethod event-dispatch! :default
  [d]
  (tre/warn "No comms dispatch handler for " d))

(reg-fx :data-event-dispatch
  (fn
    [data]
    (event-dispatch! data)))

(reg-event-fx :server-chsk-dispatch
  (fn
    [_ [_ data]]
    {:data-event-dispatch data}))

; ---- API functions -------------------
(defn send-msg! [chan-name msg]
  (ws/chsk-send! [:event/new-msg [chan-name msg]]))

(defn create-channel! [chan-name]
  (ws/chsk-send! [:event/new-chan [chan-name]]))

(defn create-user! [user]
  (ws/chsk-send! [:event/create-user [user]]))

(defn start-broadcaster! []
  (tre/info "Starting model changes broadcaster")
  (go-loop [i 0]
    (let [d (<! changes)]
      (tre/trace "Triggered change" @d)
      (dispatch [:server-data @d]))
    (recur (inc i))))

(m/defstate broadcaster :start (start-broadcaster!)
            :stop (async/close! @broadcaster))