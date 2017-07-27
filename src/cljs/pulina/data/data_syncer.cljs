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
    (puu/model {})
    ; no need for excessive amount of versions on client side, even 3 is on the safe side
    ; most likely 2 would suffice more than enough
    :limit 3))

(def changes (puu/subscribe mgr (chan)))

; ---------------------------------------
; communication event dispatching
(defmulti event-dispatch! first)

(defmethod event-dispatch! :model/all
  [[_ data]]
  (tre/debug "Received initial model data: " data)
  (puu/do-tx mgr (fn [_]
                   ; we need to convert the messages map into sorted map so that they'll get ordered correctly
                   ; the type of sorted-map is lost in the transit serialization and thus we need to recreate it here.
                   ; After this initial fumbling all the new messages will get applied to this data structure and thus
                   ; they will get ordered correctly. The reason for using a map in the first place as data structure
                   ; comes from the diffing algorithm: this allows it to just tell about single added element and
                   ; index of the insertion is not important: this reduces the amount of data sent between changesets.
                   (update
                     (:data data)
                     :channels
                     (fn [channels]
                       (map
                         (fn [chan]
                           (update
                             chan
                             :messages
                             #(into (sorted-map-by (fn [a b] (< (first a) (first b)))) %)))
                         channels))))))

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

(defn start-broadcaster! []
  (tre/info "Starting model changes broadcaster")
  (go-loop [i 0]
    (let [d (<! changes)]
      (tre/trace "Triggered change" @d)
      (dispatch [:server-data @d]))
    (recur (inc i))))

(m/defstate broadcaster :start (start-broadcaster!)
            :stop (async/close! @broadcaster))