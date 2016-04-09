(ns pulina.data.data-syncer
  (:require-macros
    [cljs.core.async.macros :as asyncm :refer [go go-loop]])
  (:require
    [mount.core :as m]
    [cljs.pprint]
    [taoensso.timbre :as tre]
    [re-frame.core :refer [dispatch]]
    [cljs.core.async :as async :refer [<! >! put! chan]]
    [taoensso.sente :as sente :refer [cb-success?]]
    [puu.model :as puu]))

(def mgr
  (puu/manager
    "chat-mgr"
    (puu/model {})))

(def changes (puu/subscribe mgr (chan)))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" {:type :auto})]
  (def chsk       chsk)
  (def ch-chsk    ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state state))

(defn init-model! []
  (tre/info "Initializing model data")
  (chsk-send! [:model/all] 8000))

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

; ---------------------------------------
; websocket communication and dispatching
(defmulti dispatch! :id)

(defmethod dispatch! :chsk/state
  [{data :?data}]
  (cond
    (:first-open? data) (init-model!)
    :default nil))

(defmethod dispatch! :chsk/recv
  [{data :?data}]
  (event-dispatch! data))

(defmethod dispatch! :default
  [d]
  (tre/warn "No dispatch handler found for " (:id d)))


; --------------------------------------
; channel listeners
(defn start-receiver! []
  (go-loop [i 0]
    (let [d (<! ch-chsk)]
      (dispatch! d))
    (recur (inc i))))

(defn start-broadcaster! []
  (go-loop [i 0]
    (let [d (<! changes)]
      (tre/trace "Triggered change" @d)
      (dispatch [:server-data @d]))
    (recur (inc i))))

(defn send-msg! [chan-name msg]
  (chsk-send! [:event/new-msg [chan-name msg]]))

(m/defstate receiver :start (start-receiver!)
                     :stop  (async/close! @receiver))

(m/defstate broadcaster :start (start-broadcaster!)
                        :stop  (async/close! @broadcaster))